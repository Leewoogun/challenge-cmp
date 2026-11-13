package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.lwg.challenge.camera.CapturedPhoto
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.resources.decodeToImageBitmap

/** 촬영한 인증 사진 미리보기. 디코드에 실패하면(Preview 픽스처 포함) 자리 표시 문구를 보여준다. */
@Composable
internal fun CapturedPhotoImage(
    photo: CapturedPhoto,
    modifier: Modifier = Modifier,
) {
    val imageBitmap = remember(photo) {
        runCatching { photo.jpegBytes.decodeToImageBitmap() }.getOrNull()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChallengeTheme.colorScheme.background.cardSub),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "촬영한 인증 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = "사진을 표시할 수 없어요",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CapturedPhotoImagePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            CapturedPhotoImage(
                photo = CapturedPhoto(jpegBytes = ByteArray(0), width = 4, height = 3),
                modifier = Modifier.aspectRatio(4f / 3f),
            )
        }
    }
}
