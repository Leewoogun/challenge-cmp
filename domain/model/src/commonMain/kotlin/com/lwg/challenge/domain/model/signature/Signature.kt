package com.lwg.challenge.domain.model.signature

/**
 * 서명 저장 규칙. **서버 검증과 반드시 같은 값이어야 한다.**
 *
 * ### 크기 상한 산정
 * 좌표 1개는 최악 `1000` (4자) 이고 점 1개는 `x,y,` = 최악 10바이트다.
 * [MAX_POINTS] 를 [MAX_STROKES] 개 획에 나눠 담아 전부 최악값으로 채우면
 * `10 × 2000` + 획 구분 `[`/`]`/`,` `3 × 100` + 헤더 약 20 ≈ **20.3 KB** 다.
 * 여유분을 더해 **API 계약 상한은 32 KB** 를 권고한다.
 * 이 산정은 `SignatureCodecTest` 의 크기 상한 테스트가 뒷받침한다.
 */
object SignatureLimits {

    /**
     * 정규화 좌표의 양자화 해상도. 좌표는 `0..GRID` 범위의 Int 다.
     *
     * 400dp 캔버스 기준 1 눈금 = 0.4dp 라 시각 임계 이하다.
     */
    const val GRID: Int = 1000

    /**
     * 직전 점과의 거리가 이 값(그리드 단위) 미만이면 버린다(간축).
     *
     * 60Hz 캡처가 만드는 중복점을 제거해 저장 크기를 억제한다.
     * 단, 획의 첫 점과 마지막 점은 간축 대상이 아니다 — 끝점이 잘리면 획이 짧아 보인다.
     */
    const val MIN_POINT_DISTANCE: Int = 3

    /** 서명 1건이 가질 수 있는 전체 점 개수 상한. 초과분은 조용히 버린다. */
    const val MAX_POINTS: Int = 2000

    /**
     * 서명 1건이 가질 수 있는 획 개수 상한. 초과분은 조용히 버린다.
     *
     * 짧은 획을 많이 쓰는 서명이 잘리지 않도록 넉넉하게 잡았다.
     */
    const val MAX_STROKES: Int = 100
}

/** 서명 획 하나. 좌표는 `0..`[SignatureLimits.GRID] 로 양자화된 정규화 좌표다. */
data class SignatureStroke(val points: List<SignaturePoint>)

/**
 * 양자화된 정규화 좌표 1점.
 *
 * @property x 가로 위치. 0 = 캔버스 왼쪽 끝, [SignatureLimits.GRID] = 오른쪽 끝
 * @property y 세로 위치. 0 = 캔버스 위쪽 끝, [SignatureLimits.GRID] = 아래쪽 끝
 */
data class SignaturePoint(val x: Int, val y: Int)

/**
 * 손글씨 서명 1건. 획들의 목록이다.
 *
 * 이미지가 아니라 벡터 좌표라 어느 해상도에서도 그대로 렌더된다.
 * 손실 단계(양자화)는 캡처 시점 한 번만 일어나므로, 이후 인코딩↔디코딩은 전단사다
 * (= 저장 표현에 대한 왕복 무손실. 원본 손가락 입력에 대한 무손실이 아니다).
 *
 * **빈 서명([isEmpty] == true)은 거부된다** — spec 수용 기준 "빈 서명(획 0개)은 거부된다".
 * 거부 판정은 이 모델이 아니라 저장을 시도하는 쪽(ViewModel / 서버)의 책임이다.
 */
data class Signature(val strokes: List<SignatureStroke>) {

    val isEmpty: Boolean get() = strokes.all { it.points.isEmpty() }

    val strokeCount: Int get() = strokes.size

    val pointCount: Int get() = strokes.sumOf { it.points.size }

    companion object {
        val Empty = Signature(emptyList())
    }
}
