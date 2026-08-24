package com.lwg.challenge.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.lwg.challenge.utils.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun PhotoCompressor(): PhotoCompressor = PhotoCompressorAndroid()

internal class PhotoCompressorAndroid : PhotoCompressor {

    override suspend fun compress(filePath: String, spec: CompressionSpec): CapturedPhoto? =
        withContext(Dispatchers.IO) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(filePath, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    Logger.e("인증 사진 크기 읽기 실패: $filePath")
                    return@withContext null
                }

                val options = BitmapFactory.Options().apply {
                    inSampleSize = PhotoScaling.sampleSize(bounds.outWidth, bounds.outHeight, spec.maxDimensionPx)
                }
                val decoded = BitmapFactory.decodeFile(filePath, options)
                if (decoded == null) {
                    Logger.e("인증 사진 디코드 실패: $filePath")
                    return@withContext null
                }

                val scaled = decoded.scaleTo(PhotoScaling.targetSize(decoded.width, decoded.height, spec.maxDimensionPx))
                val oriented = scaled.applyExifRotation(filePath)

                val encoded = oriented.encodeToTarget(spec)
                val stripped = JpegMetadata.strip(encoded.bytes)
                // T-I1 실기 검증 때 리사이즈 목표/실측 수치를 사람이 따로 재지 않기 위한 계측 —
                // iOS 와 같은 형식이어야 두 플랫폼 수치를 나란히 비교할 수 있다
                Logger.i(
                    "인증 사진 압축 실측: 원본 ${File(filePath).length()}B ${bounds.outWidth}x${bounds.outHeight}" +
                        " -> 결과 ${stripped.size}B ${oriented.width}x${oriented.height}" +
                        ", quality=${encoded.quality}, 목표 ${spec.targetBytes}B 초과=${stripped.size > spec.targetBytes}" +
                        ", EXIF제거=${stripped.size != encoded.bytes.size}",
                )
                CapturedPhoto(jpegBytes = stripped, width = oriented.width, height = oriented.height)
                    .also { oriented.recycle() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e("인증 사진 압축 실패: $filePath", e)
                null
            }
        }
}

private fun Bitmap.scaleTo(target: ScaledSize): Bitmap {
    if (width == target.width && height == target.height) return this

    val scaled = Bitmap.createScaledBitmap(this, target.width, target.height, true)
    if (scaled !== this) recycle()
    return scaled
}

/**
 * 시스템 카메라 결과물은 회전 정보를 EXIF orientation 에만 담는 경우가 많고, 재인코딩하면
 * EXIF 가 사라져 세로로 찍은 사진이 눕는다. 그래서 픽셀 자체를 돌려 놓는다.
 * 우리가 만든 파일 경로라 프레임워크 ExifInterface 로 충분하다.
 * 회전을 픽셀에 적용하므로 [JpegMetadata.strip] 이 출력 JPEG 의 EXIF 를 제거해도 방향이 유지된다.
 */
private fun Bitmap.applyExifRotation(filePath: String): Bitmap {
    val orientation = ExifInterface(filePath)
        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return this
    }

    val matrix = Matrix().apply { postRotate(degrees) }
    val rotated = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (rotated !== this) recycle()
    return rotated
}

/** 품질 루프의 최종 산출물. [quality] 는 실제 채택된 값 — 실측 계측용. */
private class EncodedJpeg(val bytes: ByteArray, val quality: Int)

private fun Bitmap.encodeToTarget(spec: CompressionSpec): EncodedJpeg {
    var bytes = ByteArray(0)
    var lastQuality = spec.minQuality
    for (quality in spec.qualitySteps()) {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        lastQuality = quality
        if (bytes.size <= spec.targetBytes) return EncodedJpeg(bytes, quality)
    }
    // 최저 품질로도 targetBytes 를 못 맞춘 경우 마지막 결과를 그대로 쓴다 —
    // null 로 버리면 사용자가 인증 자체를 못 하게 된다
    return EncodedJpeg(bytes, lastQuality)
}
