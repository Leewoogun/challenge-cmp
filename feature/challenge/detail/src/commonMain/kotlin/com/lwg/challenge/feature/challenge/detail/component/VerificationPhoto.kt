package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus

private val PhotoShape = RoundedCornerShape(16.dp)

private const val NOT_VERIFIED_TEXT = "아직 인증하지 않았어요"
private const val VERIFICATION_FAILED_TEXT = "인증하지 못했어요"
private const val LOADING_TEXT = "불러오는 중..."
private const val LOAD_FAILED_TEXT = "사진을 불러오지 못했어요"
private const val STATUS_UNKNOWN_TEXT = "인증 현황을 불러오지 못했어요"
private const val PHOTO_DELETED_TEXT = "탈퇴한 사용자의 사진은 삭제됐어요"

/**
 * 인증 사진 영역. 상태가 바뀌어도 아래 카드가 튀지 않게 높이를 160dp 로 고정한다.
 *
 * @param status 인증 현황 조회에 실패했으면 `null`. '미인증' 이 아니라 조회 실패를 드러낸다.
 * @param isLoading 인증 현황 자체를 아직 못 받은 구간. 이미지 로딩(Coil)과는 별개다.
 * @param cacheKey 교체해도 URL 이 같아 옛 사진이 돌아오므로 필수로 받는다.
 * @param photoDeleted 탈퇴로 사진이 영구 삭제된 칸. 다시 받을 대상이 없어 재시도를 열지 않는다.
 * @param onRetry [status] 가 `null` 일 때 현황 재조회용. 넘기지 않으면 문구만 남는다.
 */
@Composable
internal fun VerificationPhoto(
    status: VerificationStatus?,
    photoUrl: String?,
    cacheKey: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    photoDeleted: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(PhotoShape)
            .background(ChallengeTheme.colorScheme.background.cardSub),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> PhotoMessage(LOADING_TEXT)
            status == null -> if (onRetry == null) {
                PhotoMessage(STATUS_UNKNOWN_TEXT)
            } else {
                PhotoRetryMessage(text = STATUS_UNKNOWN_TEXT, onRetry = onRetry)
            }
            status == VerificationStatus.PENDING -> PhotoMessage(NOT_VERIFIED_TEXT)
            photoDeleted -> PhotoMessage(PHOTO_DELETED_TEXT)
            status == VerificationStatus.FAILED && photoUrl == null ->
                PhotoMessage(VERIFICATION_FAILED_TEXT)
            photoUrl == null -> PhotoMessage(LOAD_FAILED_TEXT)
            else -> RemotePhoto(url = photoUrl, cacheKey = cacheKey)
        }
    }
}

/** 로드 실패를 빈 칸으로 삼키면 URL 조인 사고가 QA 를 통과하므로 문구와 재시도를 남긴다. */
@Composable
private fun RemotePhoto(url: String, cacheKey: String?) {
    val context = LocalPlatformContext.current

    val request = remember(context, url, cacheKey) {
        ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .build()
    }

    // 같은 URL 재요청은 painter 를 새로 만들어야 해서 key 로 노드를 갈아 끼운다.
    var retryTick by remember(url, cacheKey) { mutableIntStateOf(0) }

    key(retryTick) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { CenteredSlot { PhotoMessage(LOADING_TEXT) } },
            error = { CenteredSlot { PhotoRetryMessage(text = LOAD_FAILED_TEXT, onRetry = { retryTick++ }) } },
        )
    }
}

/** SubcomposeAsyncImage 는 슬롯에 최소 크기를 강제(propagateMinConstraints)해 내용이 위로 붙으므로 한 번 감싼다. */
@Composable
private fun CenteredSlot(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun PhotoMessage(text: String) {
    Text(
        text = text,
        style = ChallengeTheme.typography.medium14,
        color = ChallengeTheme.colorScheme.text.secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun PhotoRetryMessage(text: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhotoMessage(text)

        Text(
            text = "다시 시도",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.point,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/** 탈퇴로 사진이 삭제됨 — 아래 [VerificationPhotoMissingUrlPreview] 와 문구가 달라야 한다. */
@ThemePreviews
@Composable
private fun VerificationPhotoDeletedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                photoDeleted = true,
            )
        }
    }
}

/** 인증했는데 URL 이 없는 데이터 이상 — 삭제가 아니므로 기존 로드 실패 문구가 그대로 나와야 한다. */
@ThemePreviews
@Composable
private fun VerificationPhotoMissingUrlPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                photoDeleted = false,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun VerificationPhotoNotVerifiedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = VerificationStatus.PENDING, photoUrl = null, cacheKey = null)
        }
    }
}

/** 마감까지 미제출 — 사진 없는 `FAILED`. */
@ThemePreviews
@Composable
private fun VerificationPhotoFailedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = VerificationStatus.FAILED, photoUrl = null, cacheKey = null)
        }
    }
}

@ThemePreviews
@Composable
private fun VerificationPhotoLoadingPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                isLoading = true,
            )
        }
    }
}

/** 인증 현황 조회 실패 — 사진 대신 현황을 못 받았다고 밝히고 전체 재조회를 연다. */
@ThemePreviews
@Composable
private fun VerificationPhotoStatusUnknownPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = null, photoUrl = null, cacheKey = null, onRetry = {})
        }
    }
}

/** Coil 에러 슬롯은 프리뷰에서 재현할 수 없어 같은 제약(propagateMinConstraints)의 160dp 틀에 얹는다. */
@ThemePreviews
@Composable
private fun VerificationPhotoLoadErrorPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(PhotoShape)
                    .background(ChallengeTheme.colorScheme.background.cardSub),
                propagateMinConstraints = true,
            ) {
                CenteredSlot { PhotoRetryMessage(text = LOAD_FAILED_TEXT, onRetry = {}) }
            }
        }
    }
}
