package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/forfeit 성공 응답.
 *
 * 🔴 **앱은 이 body 를 읽지 않는다.** 기권 성공 후 상세를 재조회해 화면을 세우기 때문이다 —
 * 성공 응답으로 상태를 부분 갱신하면 상세 화면을 세우는 경로가 두 벌이 된다.
 *
 * 그래도 계약 shape 을 그대로 두는 것은 [RejectChallengeResponse] 와 같은 선례다. 계약이 무엇을
 * 약속했는지가 코드에 남아야 서버가 shape 을 바꿀 때 드러난다.
 */
@Serializable
data class ForfeitChallengeResponse(
    val data: ForfeitChallengeData = ForfeitChallengeData(),
) : BaseResponse()

@Serializable
data class ForfeitChallengeData(
    val challengeId: Long = 0L,
    /** 기권은 즉시 확정이라 항상 `COMPLETED`. */
    val status: String = "",
    /** 역할 기준 판정 결과. enum 이 아니라 String 인 이유는 [ChallengeDetailData.result] 참조. */
    val result: String = "",
    /** 요청자 시점 결과. [result] 와 같은 이유로 String 이다. */
    val myResult: String = "",
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val completedAt: LocalDateTime? = null,
)
