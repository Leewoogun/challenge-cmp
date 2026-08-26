package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeResult
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 2026-08-03 로컬 실서버 `GET /api/v1/challenges/{18,21}` 실제 응답. 손으로 쓴 픽스처가 아니라
 * 서버가 실제로 보낸 본문이다. 서버가 shape 을 바꾸면 이 테스트가 깨진다.
 *
 * [ChallengeDetailMapperTest] 는 DTO 를 코드로 조립해 매퍼만 검증한다 — 그래서 DTO 바인딩 층
 * (JSON 키 이름, 날짜 포맷, 서명이 객체가 아니라 문자열로 오는지) 이 통째로 사각지대다.
 * 이 테스트는 원문 바이트에서 시작해 도메인 모델까지 한 번에 통과시켜 그 구간을 덮는다.
 *
 * IN_PROGRESS 와 PENDING 두 상태를 모두 고정한다. 처음에는 IN_PROGRESS 만 떠 뒀는데, 그건
 * 유일하게 동작하는 상태였고 화면이 실제로 여는 PENDING 은 역직렬화가 깨져 있었다.
 * 실서버에서 떴다는 사실이 대표 응답을 떴다는 뜻은 아니다.
 *
 * 세 번째 픽스처([nullMaxWireResponse])는 성격이 다르다. 앞의 둘은 실서버에서 뜬 것이라
 * "지금 존재하는 것"만 덮는다. 이건 계약 §3.1 nullable 표에서 파생한 "계약이 허용하는 것" 이고,
 * 모든 nullable 이 동시에 null 인 조합은 실서버로 만들 수 없어 backend-dev 가 서버 테스트에서
 * 합성해 넘겼다.
 */
class ChallengeDetailWireFixtureTest {

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val wireResponse =
        """{"data":{"challengeId":18,"status":"IN_PROGRESS","challengeDate":"2026-08-03","deadline":"2026-08-04 00:00:00","betContent":"커피 사기","challenger":{"userId":16,"nickname":"테스터3","profileImageUrl":null,"mission":"운동 1시간"},"opponent":{"userId":15,"nickname":"테스터2","profileImageUrl":null,"mission":"책 30페이지 읽기"},"contract":{"content":"영혼의 맹세\n\n2026년 8월 3일, 테스터3와 테스터2는\n아래의 약속을 걸고 맹세한다.\n\n· 테스터3의 미션: 운동 1시간\n· 테스터2의 미션: 책 30페이지 읽기\n· 내기: 커피 사기\n· 마감: 2026년 8월 3일 24:00\n\n서명한 맹세는 무를 수 없다.","isFinalized":true,"challengerSignature":"{\"v\":1,\"g\":1000,\"s\":[[123,456,130,460],[700,200,705,210]]}","challengerSignedAt":"2026-08-03 13:37:12","opponentSignature":"{\"v\":1,\"g\":1000,\"s\":[[123,456,130,460],[700,200,705,210]]}","opponentSignedAt":"2026-08-03 13:37:13"}},"error":false,"code":200,"message":""}"""

    // `GET /api/v1/challenges/21` — 맹세 화면이 실제로 여는 상태. 상대가 아직 맹세하지 않아
    // opponent.mission 이 명시적 null 로 온다. 서명 획 좌표만 앞 3점으로 줄였다.
    private val pendingWireResponse =
        """{"data":{"challengeId":21,"status":"PENDING","challengeDate":"2026-08-04","deadline":"2026-08-05 00:00:00","betContent":"Coffee","challenger":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":"Run5km"},"opponent":{"userId":15,"nickname":"테스터2","profileImageUrl":null,"mission":null},"contract":{"content":"영혼의 맹세","isFinalized":false,"challengerSignature":"{\"v\":1,\"g\":1000,\"s\":[[93,204,99,217,104,229]]}","challengerSignedAt":"2026-08-03 14:10:59","opponentSignature":null,"opponentSignedAt":null}},"error":false,"code":200,"message":""}"""

    // 계약 §3.1 nullable 표의 모든 항목이 동시에 null 인 "null 최대" 응답. contract 자체가 null 인
    // 챌린지는 soul-oath 도입 이전에 만들어진 것이라 계약서가 없다 — 오류가 아니라 정상 상태다.
    // result 는 IN_PROGRESS 라 명시적 null 로 온다 (키는 존재).
    private val nullMaxWireResponse =
        """{"data":{"challengeId":7,"status":"IN_PROGRESS","result":null,"challengeDate":"2026-08-03","deadline":"2026-08-04 00:00:00","betContent":"커피 사기","challenger":{"userId":1,"nickname":"이우건","profileImageUrl":null,"mission":null},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":null},"contract":null},"error":false,"code":200,"message":""}"""

    // 판정이 끝난 챌린지. 위의 두 실서버 픽스처는 result 도입 이전에 뜬 것이라 키 자체가 없고,
    // 그건 그것대로 '키 누락도 null 로 떨어진다' 를 고정한다. 값이 실린 shape 은 여기서 덮는다.
    private val completedWireResponse =
        """{"data":{"challengeId":30,"status":"COMPLETED","result":"CHALLENGER_WIN","challengeDate":"2026-08-03","deadline":"2026-08-04 00:00:00","betContent":"커피 사기","challenger":{"userId":16,"nickname":"테스터3","profileImageUrl":null,"mission":"운동 1시간"},"opponent":{"userId":15,"nickname":"테스터2","profileImageUrl":null,"mission":"책 30페이지 읽기"},"contract":null},"error":false,"code":200,"message":""}"""

    private fun parse(): ChallengeDetailResponse = json.decodeFromString(wireResponse)

    private fun parsePending(): ChallengeDetailResponse = json.decodeFromString(pendingWireResponse)

    private fun parseNullMax(): ChallengeDetailResponse = json.decodeFromString(nullMaxWireResponse)

    private fun parseCompleted(): ChallengeDetailResponse = json.decodeFromString(completedWireResponse)

    @Test
    fun `실서버 응답 원문이 ChallengeDetailResponse 로 역직렬화된다`() {
        val response = parse()

        assertEquals(200, response.code)
        assertFalse(response.error)
    }

    @Test
    fun `실서버 응답이 도메인 모델로 매핑된다`() {
        assertNotNull(parse().toChallengeDetail())
    }

    @Test
    fun `기본 필드와 날짜 포맷이 그대로 매핑된다`() {
        val detail = assertNotNull(parse().toChallengeDetail())

        assertEquals(18L, detail.challengeId)
        assertEquals("IN_PROGRESS", detail.status)
        assertEquals("커피 사기", detail.betContent)
        assertEquals(LocalDate(2026, 8, 3), detail.challengeDate)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
    }

    @Test
    fun `challenger 와 opponent 역할이 뒤집히지 않는다`() {
        val detail = assertNotNull(parse().toChallengeDetail())

        assertEquals(16L, detail.challenger.userId)
        assertEquals("테스터3", detail.challenger.nickname)
        assertEquals("운동 1시간", detail.challenger.mission)

        assertEquals(15L, detail.opponent.userId)
        assertEquals("테스터2", detail.opponent.nickname)
        assertEquals("책 30페이지 읽기", detail.opponent.mission)
    }

    @Test
    fun `양측 서명이 문자열에서 디코드되고 두 슬롯 모두 SIGNED 다`() {
        val contract = assertNotNull(assertNotNull(parse().toChallengeDetail()).contract)

        val challengerSignature = assertNotNull(contract.challengerSignature)
        val opponentSignature = assertNotNull(contract.opponentSignature)

        assertEquals(2, challengerSignature.strokeCount)
        assertEquals(4, challengerSignature.pointCount)
        assertEquals(2, opponentSignature.strokeCount)
        assertEquals(4, opponentSignature.pointCount)

        assertEquals(SignatureSlotState.SIGNED, contract.challengerSignatureState)
        assertEquals(SignatureSlotState.SIGNED, contract.opponentSignatureState)
    }

    @Test
    fun `평탄 배열이 x y 쌍으로 정확히 복원된다`() {
        val signature =
            assertNotNull(assertNotNull(assertNotNull(parse().toChallengeDetail()).contract).challengerSignature)

        assertEquals(SignaturePoint(123, 456), signature.strokes[0].points[0])
        assertEquals(SignaturePoint(130, 460), signature.strokes[0].points[1])
        assertEquals(SignaturePoint(700, 200), signature.strokes[1].points[0])
    }

    @Test
    fun `바이트 왕복 - 서버가 돌려준 서명 문자열과 코덱이 만든 문자열이 완전히 같다`() {
        val response = parse()
        val detail = assertNotNull(response.toChallengeDetail())
        val wireContract = assertNotNull(response.data.contract)
        val domainContract = assertNotNull(detail.contract)

        val roundTripMeaning =
            "서버가 저장·반환하는 서명 문자열과 SignatureCodec 이 만들어내는 문자열이 바이트 단위로 같아야 한다. " +
                "다르면 앱이 다시 저장하는 순간 서버 데이터의 표현이 바뀐다"

        assertEquals(
            assertNotNull(wireContract.challengerSignature),
            SignatureCodec.encode(assertNotNull(domainContract.challengerSignature)),
            roundTripMeaning,
        )
        assertEquals(
            assertNotNull(wireContract.opponentSignature),
            SignatureCodec.encode(assertNotNull(domainContract.opponentSignature)),
            roundTripMeaning,
        )
    }

    @Test
    fun `확정된 계약의 서명 시각이 파싱된다`() {
        val contract = assertNotNull(assertNotNull(parse().toChallengeDetail()).contract)

        assertTrue(contract.isFinalized)
        assertEquals(LocalDateTime(2026, 8, 3, 13, 37, 12), contract.challengerSignedAt)
        assertEquals(LocalDateTime(2026, 8, 3, 13, 37, 13), contract.opponentSignedAt)
    }

    @Test
    fun `PENDING 응답 원문이 역직렬화된다 - 상대 미션이 명시적 null 이어도 예외가 아니다`() {
        val response = parsePending()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(21L, response.data.challengeId)
        assertEquals("PENDING", response.data.status)
    }

    @Test
    fun `PENDING 응답이 도메인 모델로 매핑된다`() {
        assertNotNull(parsePending().toChallengeDetail())
    }

    @Test
    fun `PENDING 상대 미션은 null 로 남고 빈 문자열로 뭉개지지 않는다`() {
        val detail = assertNotNull(parsePending().toChallengeDetail())

        assertNull(
            detail.opponent.mission,
            "상대가 아직 맹세하지 않은 상태다. 빈 문자열로 바꾸면 '미션 없음'과 '빈 미션'이 같아진다",
        )
        assertNotEquals("", detail.opponent.mission)
        assertEquals("테스터2", detail.opponent.nickname)
    }

    @Test
    fun `PENDING 이어도 챌린저 미션은 그대로 온다`() {
        val detail = assertNotNull(parsePending().toChallengeDetail())

        assertEquals("Run5km", detail.challenger.mission)
        assertEquals(14L, detail.challenger.userId)
    }

    @Test
    fun `PENDING 계약은 미확정이고 챌린저만 서명돼 있다`() {
        val contract = assertNotNull(assertNotNull(parsePending().toChallengeDetail()).contract)

        assertFalse(contract.isFinalized)
        assertEquals(SignatureSlotState.SIGNED, contract.challengerSignatureState)
        assertEquals(SignatureSlotState.NOT_SIGNED, contract.opponentSignatureState)
    }

    @Test
    fun `contract 가 null 인 응답 원문이 역직렬화된다`() {
        val response = parseNullMax()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(7L, response.data.challengeId)
        assertNull(response.data.contract)
    }

    @Test
    fun `contract 가 null 이어도 도메인 모델로 매핑되고 계약서만 null 로 남는다`() {
        val detail = assertNotNull(
            parseNullMax().toChallengeDetail(),
            "계약서가 없다고 상세 전체를 버리면 안 된다. deadline 과 달리 계약서 없이도 챌린지는 성립한다",
        )

        assertNull(
            detail.contract,
            "soul-oath 이전에 만들어진 챌린지다. 빈 Contract 로 채우면 없던 맹세를 날조하게 된다",
        )
    }

    @Test
    fun `contract 가 null 이어도 최상위 필드는 그대로 매핑된다`() {
        val detail = assertNotNull(parseNullMax().toChallengeDetail())

        assertEquals(7L, detail.challengeId)
        assertEquals("IN_PROGRESS", detail.status)
        assertEquals("커피 사기", detail.betContent)
        assertEquals(LocalDate(2026, 8, 3), detail.challengeDate)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
        assertEquals("이우건", detail.challenger.nickname)
        assertEquals("테스터1", detail.opponent.nickname)
    }

    @Test
    fun `양측 미션이 모두 null 로 남고 빈 문자열로 뭉개지지 않는다`() {
        val detail = assertNotNull(parseNullMax().toChallengeDetail())

        assertNull(detail.challenger.mission)
        assertNull(detail.opponent.mission)
        assertNotEquals("", detail.challenger.mission)
        assertNotEquals("", detail.opponent.mission)
    }

    @Test
    fun `양측 profileImageUrl 이 null 로 남는다`() {
        val detail = assertNotNull(parseNullMax().toChallengeDetail())

        assertNull(detail.challenger.profileImageUrl)
        assertNull(detail.opponent.profileImageUrl)
    }

    @Test
    fun `result 가 명시적 null 인 원문 - 역직렬화되고 도메인도 null 이다`() {
        assertNull(parseNullMax().data.result)
        assertNull(assertNotNull(parseNullMax().toChallengeDetail()).result)
    }

    @Test
    fun `result 키가 없는 원문도 null 로 떨어진다`() {
        assertNull(assertNotNull(parse().toChallengeDetail()).result)
        assertNull(assertNotNull(parsePending().toChallengeDetail()).result)
    }

    @Test
    fun `COMPLETED 원문이 역직렬화되고 result 가 문자열로 온다`() {
        val response = parseCompleted()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals("COMPLETED", response.data.status)
        assertEquals("CHALLENGER_WIN", response.data.result)
    }

    @Test
    fun `COMPLETED 원문의 result 가 역할 기준 그대로 매핑된다`() {
        val detail = assertNotNull(parseCompleted().toChallengeDetail())

        assertEquals(
            ChallengeResult.CHALLENGER_WIN,
            detail.result,
            "'나 / 상대' 로 뒤집지 않는다. 관점 변환은 화면 계층의 몫이다",
        )
        assertEquals(30L, detail.challengeId)
        assertEquals("COMPLETED", detail.status)
        assertEquals(16L, detail.challenger.userId)
        assertEquals(15L, detail.opponent.userId)
    }
}
