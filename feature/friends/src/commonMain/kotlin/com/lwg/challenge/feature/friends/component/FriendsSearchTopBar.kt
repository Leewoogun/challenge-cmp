package com.lwg.challenge.feature.friends.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 검색 화면 전용 상단바. design.md §B.6/§C — ← 뒤로가기 + 닉네임 입력.
 *
 * 입력 박스는 [BasicTextField] + decorationBox 로 secondary 배경 / R12 / leading Search /
 * trailing clear(X) 를 직접 구성한다(Material3 TextField 의 고정 높이·indicator 회피).
 */
@Composable
internal fun FriendsSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChallengeTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기",
                    modifier = Modifier.size(20.dp),
                    tint = ChallengeTheme.colorScheme.onBackground,
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = ChallengeTheme.typography.medium14.copy(
                    color = ChallengeTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(ChallengeTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChallengeTheme.colorScheme.secondary)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "닉네임 검색",
                                    style = ChallengeTheme.typography.medium14,
                                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                        if (query.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "검색어 지우기",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onQueryChange("") },
                                tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun FriendsSearchTopBarPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchTopBar(
                query = "우건",
                onQueryChange = {},
                onBack = {},
            )
        }
    }
}
