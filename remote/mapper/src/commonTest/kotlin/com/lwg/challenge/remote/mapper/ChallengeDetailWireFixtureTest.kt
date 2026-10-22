package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `GET /api/v1/challenges/{id}` 원문 JSON 에서 도메인까지 한 번에 통과시킨다.
 *
 * [inProgressWireResponse] · [completedWireResponse] · [notSignedWireResponse] 는 2026-10-07 로컬 서버에서
 * 이우건(user 1) 토큰으로 뜬 실제 응답이다. [nullMaxWireResponse] · [forfeitedWireResponse] 는 실서버로
 * 만들기 어려운 조합이라 같은 shape 으로 합성했다.
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

    private val inProgressWireResponse =
        """{"data":{"challengeId":33,"status":"IN_PROGRESS","myResult":null,"forfeitedBy":null,"challengeDate":"2026-10-07","deadline":"2026-10-08 00:00:00","betContent":"커피","me":{"nickname":"이우건","profileImageUrl":"http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg","mission":"미션입니다"},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":"내 미션"},"contract":{"content":"영혼의 맹세\n\n2026년 10월 7일, 이우건과 테스터1은\n아래의 약속을 걸고 맹세한다.\n\n· 이우건의 미션: 미션입니다\n· 테스터1의 미션: 내 미션\n· 내기: 커피\n· 마감: 2026년 10월 7일 24:00\n\n서명한 맹세는 무를 수 없다.","isFinalized":true,"mySignature":"{\"v\":1,\"g\":1000,\"s\":[[353,682,363,627,376,584,395,535,416,483,438,431,459,385,484,336,507,295,526,267,544,244,557,229,568,219,577,215,583,216,587,221,590,230,596,260,598,290,600,328,600,370,600,415,600,455,599,502,600,549,600,590,600,629,602,665,604,696,607,718,610,735,614,747,618,754,627,757,639,752,653,740,671,724,690,706,707,687,726,665,747,639,766,614,784,586,807,554,829,524,851,498,872,480,892,472,911,472,926,479,938,491,948,508,957,531,963,559,969,589,974,623,978,668,985,756,986,758]]}","mySignedAt":"2026-10-07 10:51:37","opponentSignature":"{\"v\":1,\"g\":1000,\"s\":[[220,476,222,491,227,522,248,641,262,701,279,752,296,780,317,768,351,684,396,563,442,466,475,410,491,387,503,380,515,384,535,416,546,455,563,562,578,701,594,805,604,854,612,867,627,859,652,816,684,756,709,706,734,662,761,634,792,627,829,622,868,608,913,594,946,591,948,591]]}","opponentSignedAt":"2026-10-07 11:00:31"}},"error":false,"code":200,"message":""}"""

    private val completedWireResponse =
        """{"data":{"challengeId":31,"status":"COMPLETED","myResult":"LOSE","forfeitedBy":null,"challengeDate":"2026-09-02","deadline":"2026-09-03 00:00:00","betContent":"사진 변경 테스트","me":{"nickname":"이우건","profileImageUrl":"http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg","mission":"사진 변경 이우건"},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":"사진 변경"},"contract":{"content":"영혼의 맹세\n\n2026년 9월 2일, 테스터1과 이우건은\n아래의 약속을 걸고 맹세한다.\n\n· 테스터1의 미션: 사진 변경\n· 이우건의 미션: 사진 변경 이우건\n· 내기: 사진 변경 테스트\n· 마감: 2026년 9월 2일 24:00\n\n서명한 맹세는 무를 수 없다.","isFinalized":true,"mySignature":"{\"v\":1,\"g\":1000,\"s\":[[102,413,116,450,125,483,137,524,150,567,163,608,177,652,189,687,201,717,213,740,227,762,241,777,255,786,269,787,283,778,296,765,311,739,326,708,342,673,357,634,373,591,388,548,401,504,412,455,420,407,425,356,428,307,429,260,429,221,429,188,430,165,431,151,433,148,443,145,457,154,476,183,497,226,521,278,546,339,570,398,594,464,617,528,638,586,656,641,672,688,686,730,697,765,706,789,712,805,717,815,723,821,728,822,733,815,740,797,747,768,751,735,753,696,755,653,755,652]]}","mySignedAt":"2026-09-02 12:45:31","opponentSignature":"{\"v\":1,\"g\":1000,\"s\":[[344,716,339,719,339,716,339,709,342,640,367,453,412,281,445,177,462,147,475,139,483,151,489,207,493,312,496,461,509,604,527,708,546,759,567,771,590,753,621,704,655,655,689,616,718,576,742,552,757,559,770,589,784,639,795,681,803,705,823,700,845,683]]}","opponentSignedAt":"2026-09-02 12:45:11"}},"error":false,"code":200,"message":""}"""

    // 내가 수락하지 않은 채 만료된 챌린지 — 내 미션과 서명이 없다.
    private val notSignedWireResponse =
        """{"data":{"challengeId":24,"status":"EXPIRED","myResult":null,"forfeitedBy":null,"challengeDate":"2026-08-07","deadline":"2026-08-08 00:00:00","betContent":"fcm","me":{"nickname":"이우건","profileImageUrl":"http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg","mission":null},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":"푸쉬"},"contract":{"content":"영혼의 맹세\n\n2026년 8월 7일, 테스터1과 이우건은\n아래의 약속을 걸고 맹세한다.\n\n· 테스터1의 미션: 푸쉬\n· 이우건의 미션: (수락 시 기재)\n· 내기: fcm\n· 마감: 2026년 8월 7일 24:00\n\n서명한 맹세는 무를 수 없다.","isFinalized":false,"mySignature":null,"mySignedAt":null,"opponentSignature":"{\"v\":1,\"g\":1000,\"s\":[[119,710,124,697,137,644,154,546,166,462,179,373,201,275,229,179,259,108,290,91,322,109,365,164,406,250,447,352,485,469,513,573,537,648,557,696,576,724,597,735,624,721,649,679,661,639,687,534,696,497]]}","opponentSignedAt":"2026-08-07 14:24:05"}},"error":false,"code":200,"message":""}"""

    private val nullMaxWireResponse =
        """{"data":{"challengeId":7,"status":"IN_PROGRESS","myResult":null,"forfeitedBy":null,"challengeDate":"2026-08-03","deadline":"2026-08-04 00:00:00","betContent":"커피 사기","me":{"nickname":"이우건","profileImageUrl":null,"mission":null},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":null},"contract":null},"error":false,"code":200,"message":""}"""

    private val forfeitedWireResponse =
        """{"data":{"challengeId":33,"status":"COMPLETED","myResult":"WIN","forfeitedBy":"OPPONENT","challengeDate":"2026-10-07","deadline":"2026-10-08 00:00:00","betContent":"커피","me":{"nickname":"이우건","profileImageUrl":"http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg","mission":"미션입니다"},"opponent":{"userId":14,"nickname":"테스터1","profileImageUrl":null,"mission":"내 미션"},"contract":{"content":"영혼의 맹세\n\n2026년 10월 7일, 이우건과 테스터1은\n아래의 약속을 걸고 맹세한다.\n\n· 이우건의 미션: 미션입니다\n· 테스터1의 미션: 내 미션\n· 내기: 커피\n· 마감: 2026년 10월 7일 24:00\n\n서명한 맹세는 무를 수 없다.","isFinalized":true,"mySignature":"{\"v\":1,\"g\":1000,\"s\":[[353,682,363,627,376,584,395,535,416,483,438,431,459,385,484,336,507,295,526,267,544,244,557,229,568,219,577,215,583,216,587,221,590,230,596,260,598,290,600,328,600,370,600,415,600,455,599,502,600,549,600,590,600,629,602,665,604,696,607,718,610,735,614,747,618,754,627,757,639,752,653,740,671,724,690,706,707,687,726,665,747,639,766,614,784,586,807,554,829,524,851,498,872,480,892,472,911,472,926,479,938,491,948,508,957,531,963,559,969,589,974,623,978,668,985,756,986,758]]}","mySignedAt":"2026-10-07 10:51:37","opponentSignature":"{\"v\":1,\"g\":1000,\"s\":[[220,476,222,491,227,522,248,641,262,701,279,752,296,780,317,768,351,684,396,563,442,466,475,410,491,387,503,380,515,384,535,416,546,455,563,562,578,701,594,805,604,854,612,867,627,859,652,816,684,756,709,706,734,662,761,634,792,627,829,622,868,608,913,594,946,591,948,591]]}","opponentSignedAt":"2026-10-07 11:00:31"}},"error":false,"code":200,"message":""}"""

    private fun parse(raw: String): ChallengeDetailResponse = json.decodeFromString(raw)

    @Test
    fun `진행 중 원문이 역직렬화되고 기본 필드와 날짜 포맷이 매핑된다`() {
        val response = parse(inProgressWireResponse)
        assertEquals(200, response.code)
        assertFalse(response.error)

        val detail = assertNotNull(response.toChallengeDetail())
        assertEquals(33L, detail.challengeId)
        assertEquals("IN_PROGRESS", detail.status)
        assertEquals("커피", detail.betContent)
        assertEquals(LocalDate(2026, 10, 7), detail.challengeDate)
        assertEquals(LocalDateTime(2026, 10, 8, 0, 0, 0), detail.deadline)
        assertNull(detail.myResult)
        assertNull(detail.forfeitedBy)
    }

    @Test
    fun `me 와 opponent 가 그대로 매핑된다`() {
        val detail = assertNotNull(parse(inProgressWireResponse).toChallengeDetail())

        assertEquals("이우건", detail.me.nickname)
        assertEquals("미션입니다", detail.me.mission)
        assertEquals("테스터1", detail.opponent.nickname)
        assertEquals("내 미션", detail.opponent.mission)
        assertNull(detail.opponent.profileImageUrl)
    }

    @Test
    fun `양측 서명이 디코드되고 두 슬롯 모두 SIGNED 다`() {
        val contract = assertNotNull(assertNotNull(parse(inProgressWireResponse).toChallengeDetail()).contract)

        assertTrue(contract.isFinalized)
        assertNotNull(contract.mySignature)
        assertNotNull(contract.opponentSignature)
        assertEquals(SignatureSlotState.SIGNED, contract.mySignatureState)
        assertEquals(SignatureSlotState.SIGNED, contract.opponentSignatureState)
        assertEquals(LocalDateTime(2026, 10, 7, 10, 51, 37), contract.mySignedAt)
        assertEquals(LocalDateTime(2026, 10, 7, 11, 0, 31), contract.opponentSignedAt)
    }

    @Test
    fun `바이트 왕복 - 서버가 돌려준 서명 문자열과 코덱이 만든 문자열이 같다`() {
        val response = parse(inProgressWireResponse)
        val wireContract = assertNotNull(response.data.contract)
        val domainContract = assertNotNull(assertNotNull(response.toChallengeDetail()).contract)

        // 다르면 앱이 다시 저장하는 순간 서버 데이터의 표현이 바뀐다.
        assertEquals(
            assertNotNull(wireContract.mySignature),
            SignatureCodec.encode(assertNotNull(domainContract.mySignature)),
        )
        assertEquals(
            assertNotNull(wireContract.opponentSignature),
            SignatureCodec.encode(assertNotNull(domainContract.opponentSignature)),
        )
    }

    @Test
    fun `완료 원문의 myResult 가 내 시점 결과로 매핑된다`() {
        val detail = assertNotNull(parse(completedWireResponse).toChallengeDetail())

        assertEquals("COMPLETED", detail.status)
        assertEquals(ChallengeOutcome.LOSE, detail.myResult)
        assertNull(detail.forfeitedBy)
    }

    @Test
    fun `내 서명이 없으면 내 슬롯은 NOT_SIGNED 이고 내 미션은 null 로 남는다`() {
        val detail = assertNotNull(parse(notSignedWireResponse).toChallengeDetail())
        val contract = assertNotNull(detail.contract)

        assertNull(detail.me.mission)
        assertFalse(contract.isFinalized)
        assertEquals(SignatureSlotState.NOT_SIGNED, contract.mySignatureState)
        assertEquals(SignatureSlotState.SIGNED, contract.opponentSignatureState)
    }

    @Test
    fun `nullable 이 모두 null 인 원문도 매핑되고 계약서만 null 로 남는다`() {
        val detail = assertNotNull(parse(nullMaxWireResponse).toChallengeDetail())

        assertNull(detail.contract)
        assertNull(detail.me.mission)
        assertNull(detail.opponent.mission)
        assertNull(detail.me.profileImageUrl)
        assertNull(detail.myResult)
        assertNull(detail.forfeitedBy)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
    }

    @Test
    fun `기권 원문의 forfeitedBy 가 ChallengeSide 로 매핑된다`() {
        val detail = assertNotNull(parse(forfeitedWireResponse).toChallengeDetail())

        assertEquals(ChallengeSide.OPPONENT, detail.forfeitedBy)
        assertEquals(ChallengeOutcome.WIN, detail.myResult)
    }

    @Test
    fun `예전 역할 기준 shape 이 오면 빈 값으로 넘어가지 않고 파싱이 실패한다`() {
        val legacy = """{"data":{"challengeId":18,"status":"IN_PROGRESS","challengeDate":"2026-08-03","deadline":"2026-08-04 00:00:00","betContent":"커피 사기","challenger":{"userId":16,"nickname":"테스터3","profileImageUrl":null,"mission":"운동 1시간"},"opponent":{"userId":15,"nickname":"테스터2","profileImageUrl":null,"mission":"책 30페이지 읽기"},"contract":null},"error":false,"code":200,"message":""}"""

        assertFailsWith<SerializationException> {
            json.decodeFromString<ChallengeDetailResponse>(legacy)
        }
    }
}
