package com.lwg.challenge.camera

/**
 * 촬영 사진의 업로드 전 축소·재압축 파라미터.
 */
data class CompressionSpec(
    val maxDimensionPx: Int,
    val targetBytes: Int,
    val initialQuality: Int,
    val minQuality: Int,
    val qualityStep: Int,
) {

    /** initialQuality 부터 minQuality 까지 qualityStep 씩 낮춘 시도 순서. */
    internal fun qualitySteps(): List<Int> {
        val step = qualityStep.coerceAtLeast(1)
        val steps = mutableListOf<Int>()
        var quality = initialQuality
        while (quality > minQuality) {
            steps += quality
            quality -= step
        }
        steps += minQuality
        return steps
    }

    companion object {

        /** 인증 사진 기본값. targetBytes 는 ADR-0011 의 "장당 ~500KB" 목표다. */
        val VERIFICATION = CompressionSpec(
            maxDimensionPx = 1440,
            targetBytes = 500 * 1024,
            initialQuality = 85,
            minQuality = 50,
            qualityStep = 10,
        )
    }
}
