@file:OptIn(ExperimentalForeignApi::class)

package com.lwg.challenge.camera

import com.lwg.challenge.utils.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

actual fun PhotoCompressor(): PhotoCompressor = PhotoCompressorIos()

internal class PhotoCompressorIos : PhotoCompressor {

    // iOS 에는 Dispatchers.IO 가 없다
    override suspend fun compress(filePath: String, spec: CompressionSpec): CapturedPhoto? =
        withContext(Dispatchers.Default) {
            // 계측 로그용 원본 크기 — attributesOfItemAtPath 접근이 번잡해 압축 전에 한 번만 읽어 둔다
            val originalBytes = (NSFileManager.defaultManager
                .attributesOfItemAtPath(filePath, error = null)
                ?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L

            // UIImage 가 EXIF 방향을 이미 반영하고 drawInRect 도 그 방향대로 그린다 —
            // Android 구현과 달리 수동 회전이 없는 이유다.
            // 회전이 픽셀에 구워지므로 JpegMetadata.strip 이 출력 JPEG 의 EXIF 를 제거해도 방향이 유지된다
            val source = UIImage.imageWithContentsOfFile(filePath)
            if (source == null) {
                Logger.e("인증 사진 로드 실패: $filePath")
                return@withContext null
            }

            val (srcWidth, srcHeight) = source.size.useContents { width.toInt() to height.toInt() }
            val target = PhotoScaling.targetSize(srcWidth, srcHeight, spec.maxDimensionPx)

            UIGraphicsBeginImageContextWithOptions(
                CGSizeMake(target.width.toDouble(), target.height.toDouble()),
                false,
                1.0,
            )
            source.drawInRect(CGRectMake(0.0, 0.0, target.width.toDouble(), target.height.toDouble()))
            val resized = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            if (resized == null) {
                Logger.e("인증 사진 리사이즈 실패: $filePath")
                return@withContext null
            }

            val encoded = resized.encodeToTarget(spec)
            if (encoded == null) {
                Logger.e("인증 사진 JPEG 인코딩 실패: $filePath")
                return@withContext null
            }
            val jpegBytes = encoded.data.toByteArray()
            val stripped = JpegMetadata.strip(jpegBytes)
            // T-I1 실기 검증 때 리사이즈 목표/실측 수치를 사람이 따로 재지 않기 위한 계측 —
            // Android 와 같은 형식이어야 두 플랫폼 수치를 나란히 비교할 수 있다
            Logger.i(
                "인증 사진 압축 실측: 원본 ${originalBytes}B ${srcWidth}x${srcHeight}" +
                    " -> 결과 ${stripped.size}B ${target.width}x${target.height}" +
                    ", quality=${encoded.quality}, 목표 ${spec.targetBytes}B 초과=${stripped.size > spec.targetBytes}" +
                    ", EXIF제거=${stripped.size != jpegBytes.size}",
            )
            CapturedPhoto(jpegBytes = stripped, width = target.width, height = target.height)
        }
}

/** 품질 루프의 최종 산출물. [quality] 는 실제 채택된 값 — 실측 계측용. */
private class EncodedJpeg(val data: NSData, val quality: Int)

private fun UIImage.encodeToTarget(spec: CompressionSpec): EncodedJpeg? {
    var data: NSData? = null
    var lastQuality = spec.minQuality
    for (quality in spec.qualitySteps()) {
        data = UIImageJPEGRepresentation(this, quality / 100.0)
        lastQuality = quality
        if (data != null && data.length.toInt() <= spec.targetBytes) return EncodedJpeg(data, quality)
    }
    // 최저 품질로도 targetBytes 를 못 맞춘 경우 마지막 결과를 그대로 쓴다 (Android 와 동일 판단)
    return data?.let { EncodedJpeg(it, lastQuality) }
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}
