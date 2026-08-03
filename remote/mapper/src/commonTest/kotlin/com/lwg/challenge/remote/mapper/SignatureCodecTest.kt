package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignatureLimits.GRID
import com.lwg.challenge.domain.model.signature.SignatureLimits.MAX_POINTS
import com.lwg.challenge.domain.model.signature.SignatureLimits.MAX_STROKES
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignatureCodecTest {

    private fun stroke(vararg points: Pair<Int, Int>) =
        SignatureStroke(points.map { (x, y) -> SignaturePoint(x, y) })

    private val signature = Signature(
        strokes = listOf(
            stroke(123 to 456, 130 to 460, 200 to 500),
            stroke(700 to 200, 705 to 210),
            stroke(820 to 400),
        ),
    )

    @Test
    fun `왕복 무손실 - 여러 획 여러 점이 원본과 정확히 동일하게 복원된다`() {
        val decoded = SignatureCodec.decode(SignatureCodec.encode(signature))

        assertEquals(signature, decoded)
    }

    @Test
    fun `인코딩 결과가 계약에 명시한 저장 포맷과 일치한다`() {
        val encoded = SignatureCodec.encode(
            Signature(listOf(stroke(123 to 456, 130 to 460), stroke(700 to 200, 705 to 210))),
        )

        assertEquals("""{"v":1,"g":1000,"s":[[123,456,130,460],[700,200,705,210]]}""", encoded)
    }

    @Test
    fun `빈 서명 왕복 - Empty 도 그대로 복원된다`() {
        val encoded = SignatureCodec.encode(Signature.Empty)

        assertEquals("""{"v":1,"g":1000,"s":[]}""", encoded)
        assertEquals(Signature.Empty, SignatureCodec.decode(encoded))
    }

    @Test
    fun `점이 없는 획도 왕복에서 유지된다`() {
        val emptyStroke = Signature(listOf(SignatureStroke(emptyList())))

        assertEquals(emptyStroke, SignatureCodec.decode(SignatureCodec.encode(emptyStroke)))
    }

    @Test
    fun `경계 좌표 - 0 과 GRID 가 정확히 보존된다`() {
        val boundary = Signature(
            listOf(stroke(0 to 0, GRID to GRID, 0 to GRID, GRID to 0)),
        )

        assertEquals(boundary, SignatureCodec.decode(SignatureCodec.encode(boundary)))
    }

    @Test
    fun `잘못된 입력 - 빈 문자열은 null`() {
        assertNull(SignatureCodec.decode(""))
    }

    @Test
    fun `잘못된 입력 - JSON 이 아니면 null`() {
        assertNull(SignatureCodec.decode("not json"))
    }

    @Test
    fun `잘못된 입력 - 획 길이가 홀수면 null`() {
        assertNull(SignatureCodec.decode("""{"v":1,"g":1000,"s":[[1,2,3]]}"""))
    }

    @Test
    fun `잘못된 입력 - 여러 획 중 하나만 홀수여도 null`() {
        assertNull(SignatureCodec.decode("""{"v":1,"g":1000,"s":[[1,2],[3,4,5]]}"""))
    }

    @Test
    fun `잘못된 입력 - g 가 0 이면 null`() {
        assertNull(SignatureCodec.decode("""{"v":1,"g":0,"s":[[1,2]]}"""))
    }

    @Test
    fun `잘못된 입력 - g 가 음수면 null`() {
        assertNull(SignatureCodec.decode("""{"v":1,"g":-1000,"s":[[1,2]]}"""))
    }

    @Test
    fun `잘못된 입력 - 알 수 없는 v 면 null`() {
        assertNull(SignatureCodec.decode("""{"v":2,"g":1000,"s":[[1,2]]}"""))
    }

    @Test
    fun `잘못된 입력 - 필수 필드가 빠지면 null`() {
        assertNull(SignatureCodec.decode("""{"v":1,"s":[[1,2]]}"""))
    }

    @Test
    fun `g 재환산 - 절반 해상도로 인코딩된 좌표가 GRID 기준으로 스케일된다`() {
        val decoded = SignatureCodec.decode("""{"v":1,"g":500,"s":[[0,250,500,125]]}""")

        assertEquals(
            Signature(listOf(stroke(0 to 500, 1000 to 250))),
            decoded,
        )
    }

    @Test
    fun `g 재환산 - 더 큰 해상도는 반올림해 축소된다`() {
        val decoded = SignatureCodec.decode("""{"v":1,"g":4000,"s":[[4000,1000,3,2]]}""")

        assertEquals(
            Signature(listOf(stroke(1000 to 250, 1 to 1))),
            decoded,
        )
    }

    @Test
    fun `범위를 벗어난 좌표는 0과 GRID 로 잘린다`() {
        val decoded = SignatureCodec.decode("""{"v":1,"g":1000,"s":[[-50,5000]]}""")

        assertEquals(Signature(listOf(stroke(0 to GRID))), decoded)
    }

    @Test
    fun `크기 상한 - MAX_POINTS 를 최악값으로 채워도 32KB 미만이다`() {
        val worstCase = worstCaseSignature()

        val encoded = SignatureCodec.encode(worstCase)

        assertEquals(MAX_POINTS, worstCase.pointCount)
        assertEquals(MAX_STROKES, worstCase.strokeCount)
        // 실측 20,222자. 20,200 하한은 "정말 최악값으로 채웠는지" 를 지키는 가드다.
        assertTrue(encoded.length > 20_200, "worst case too small: ${encoded.length}")
        assertTrue(encoded.length < 32 * 1024, "worst case exceeds 32KB: ${encoded.length}")
    }

    @Test
    fun `크기 상한 - 최악값 서명도 왕복 무손실이다`() {
        val worstCase = worstCaseSignature()

        assertEquals(worstCase, SignatureCodec.decode(SignatureCodec.encode(worstCase)))
    }

    /** 모든 좌표가 4자리(1000)인 [MAX_POINTS] 점을 [MAX_STROKES] 개 획에 고르게 나눈 최악 입력. */
    private fun worstCaseSignature(): Signature = Signature(
        strokes = List(MAX_STROKES) { index ->
            val count = MAX_POINTS / MAX_STROKES + if (index < MAX_POINTS % MAX_STROKES) 1 else 0
            SignatureStroke(List(count) { SignaturePoint(GRID, GRID) })
        },
    )
}
