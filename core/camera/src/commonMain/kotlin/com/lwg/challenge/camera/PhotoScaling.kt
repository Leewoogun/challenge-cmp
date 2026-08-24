package com.lwg.challenge.camera

import kotlin.math.roundToInt

internal data class ScaledSize(val width: Int, val height: Int)

/**
 * 크기 계산만 공통으로 분리한 순수 함수. 플랫폼 압축 코드를 얇게 유지하고
 * 이 모듈에서 유일하게 플랫폼 없이 테스트 가능한 지점을 만든다.
 */
internal object PhotoScaling {

    /** BitmapFactory inSampleSize 용. 2 의 거듭제곱이며, 축소 후에도 maxDimensionPx 이상을 유지하는 최대값. */
    fun sampleSize(srcWidth: Int, srcHeight: Int, maxDimensionPx: Int): Int {
        if (srcWidth <= 0 || srcHeight <= 0 || maxDimensionPx <= 0) return 1

        val longSide = maxOf(srcWidth, srcHeight)
        var sample = 1
        while (longSide / (sample * 2) >= maxDimensionPx) {
            sample *= 2
        }
        return sample
    }

    /** 종횡비를 유지하며 긴 변을 maxDimensionPx 에 맞춘다. 원본이 이미 작으면 원본 크기 그대로 — 확대하지 않는다. */
    fun targetSize(srcWidth: Int, srcHeight: Int, maxDimensionPx: Int): ScaledSize {
        val width = maxOf(srcWidth, 1)
        val height = maxOf(srcHeight, 1)
        val longSide = maxOf(width, height)
        if (maxDimensionPx <= 0 || longSide <= maxDimensionPx) return ScaledSize(width, height)

        val scale = maxDimensionPx.toDouble() / longSide
        // 극단적인 종횡비에서 짧은 변이 0 으로 떨어지면 Bitmap 생성 자체가 실패하므로 하한 1 을 보장한다
        return ScaledSize(
            width = maxOf((width * scale).roundToInt(), 1),
            height = maxOf((height * scale).roundToInt(), 1),
        )
    }
}
