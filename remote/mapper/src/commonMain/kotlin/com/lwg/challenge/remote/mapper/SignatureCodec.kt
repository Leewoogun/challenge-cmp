package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignatureLimits.GRID
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

/**
 * 서명을 DB TEXT 컬럼에 담을 문자열로 인코딩/디코딩한다.
 *
 * 저장 포맷:
 * ```json
 * {"v":1,"g":1000,"s":[[123,456,130,460],[700,200,705,210]]}
 * ```
 * - `v` — 포맷 버전. 알 수 없는 버전은 디코드하지 않는다
 * - `g` — 인코딩 당시의 그리드 해상도. 디코더가 [GRID] 하드코딩에 의존하지 않게 한다
 * - `s` — 획 배열. 각 획은 `x,y` 가 번갈아 나오는 **평탄 Int 배열**
 *
 * 평탄 배열은 `[[x,y],...]` 대비 점당 2바이트를 아낀다(800점이면 1.6KB). 그 대가로
 * 디코드 시 길이가 짝수인지 검증해야 한다.
 */
object SignatureCodec {

    private const val FORMAT_VERSION = 1

    // 알 수 없는 키는 무시한다 — 호환성 판단은 v 로만 한다. 서버가 부가 필드를 먼저 붙여도
    // 구버전 앱이 서명을 통째로 못 읽는 일은 없어야 한다.
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(signature: Signature): String {
        val payload = SignaturePayload(
            version = FORMAT_VERSION,
            grid = GRID,
            strokes = signature.strokes.map { stroke ->
                ArrayList<Int>(stroke.points.size * 2).apply {
                    stroke.points.forEach { point ->
                        add(point.x)
                        add(point.y)
                    }
                }
            },
        )
        return json.encodeToString(payload)
    }

    /**
     * 실패 시 예외 대신 `null` 을 돌려준다(프로젝트의 파싱 실패 정책).
     *
     * 잘못된 JSON / 홀수 길이 획 / `g <= 0` / 알 수 없는 `v` 는 모두 `null` 이다.
     *
     * `SignatureLimits.MAX_POINTS` 등 크기 상한은 여기서 검사하지 않는다 — 저장 시점의
     * 입력 검증 규칙이지, 이미 저장된 데이터를 못 읽게 만들 이유가 아니다.
     */
    fun decode(text: String): Signature? = runCatching {
        val payload = json.decodeFromString<SignaturePayload>(text)
        require(payload.version == FORMAT_VERSION)
        require(payload.grid > 0)
        Signature(payload.strokes.map { flat -> flat.toStroke(payload.grid) })
    }.getOrNull()

    private fun List<Int>.toStroke(sourceGrid: Int): SignatureStroke {
        require(size % 2 == 0)
        val points = ArrayList<SignaturePoint>(size / 2)
        for (index in indices step 2) {
            points += SignaturePoint(
                x = this[index].rescale(sourceGrid),
                y = this[index + 1].rescale(sourceGrid),
            )
        }
        return SignatureStroke(points)
    }

    /** 다른 그리드로 인코딩된 좌표를 현재 [GRID] 기준으로 환산한다. */
    private fun Int.rescale(sourceGrid: Int): Int =
        if (sourceGrid == GRID) {
            coerceIn(0, GRID)
        } else {
            (toDouble() * GRID / sourceGrid).roundToInt().coerceIn(0, GRID)
        }
}

@Serializable
private data class SignaturePayload(
    @SerialName("v") val version: Int,
    @SerialName("g") val grid: Int,
    @SerialName("s") val strokes: List<List<Int>>,
)
