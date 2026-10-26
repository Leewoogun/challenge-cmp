package com.lwg.challenge.feature.mypage.archive

import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import kotlinx.datetime.LocalDate

/** 보관함 카드 프리뷰 픽스처. */
internal fun archivePreviewItem(
    challengeId: Long = 1L,
    challengeDate: LocalDate = LocalDate(2026, 8, 14),
    opponentNickname: String = "준혁",
    myMission: String = "헬스장 가기",
    opponentMission: String = "금주",
    bet: String = "치킨 한 마리",
    myResult: ChallengeOutcome = ChallengeOutcome.WIN,
): ChallengeHistory = ChallengeHistory(
    challengeId = challengeId,
    challengeDate = challengeDate,
    opponentNickname = opponentNickname,
    myMission = myMission,
    opponentMission = opponentMission,
    bet = bet,
    myResult = myResult,
)

/** 2026년 8월 한 달치. 결과 4종, 탈퇴한 상대, 말일(8/31) 건이 모두 들어 있다. */
internal val PreviewArchiveHistories: List<ChallengeHistory> = listOf(
    archivePreviewItem(1L, LocalDate(2026, 8, 31), "준혁", "헬스장 가기", "금주", "치킨 한 마리", ChallengeOutcome.WIN),
    archivePreviewItem(2L, LocalDate(2026, 8, 14), "민지", "물 2L 마시기", "10000보 걷기", "점심 사기 🍚", ChallengeOutcome.LOSE),
    archivePreviewItem(3L, LocalDate(2026, 8, 9), "도윤", "아침 6시 기상하기", "영어 단어 30개 외우기", "치킨 사기 🍗", ChallengeOutcome.DRAW),
    archivePreviewItem(4L, LocalDate(2026, 8, 3), "지훈", "야식 참기", "일기 쓰기", "떡볶이 사기 🍢", ChallengeOutcome.BOTH_LOSE),
    archivePreviewItem(5L, LocalDate(2026, 8, 1), "탈퇴한 사용자", "코딩 2시간", "러닝 5km", "아이스크림 사기 🍦", ChallengeOutcome.WIN),
)

/** 닉네임·미션·내기가 전부 긴 경우. 카드 헤더에서 날짜가 밀리지 않는지 본다. */
internal val PreviewLongTextArchiveHistories: List<ChallengeHistory> = listOf(
    archivePreviewItem(
        challengeId = 7L,
        challengeDate = LocalDate(2026, 8, 31),
        opponentNickname = "오늘도지지않는불꽃같은도전자김준혁입니다",
        myMission = "새벽 다섯 시에 일어나서 헬스장 가서 유산소 30분 웨이트 60분 하기",
        opponentMission = "퇴근하고 집에 오는 길에 편의점 들르지 않고 곧장 집으로 가기",
        bet = "제일 비싼 한우 오마카세 사주기 🥩",
        myResult = ChallengeOutcome.LOSE,
    ),
)
