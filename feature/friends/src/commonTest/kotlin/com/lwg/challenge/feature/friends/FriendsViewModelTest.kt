package com.lwg.challenge.feature.friends

import app.cash.turbine.test
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init 시 uiState 는 Data 로 진입한다`() = runTest {
        val viewModel = FriendsViewModel()
        assertEquals(FriendsUiState.Data, viewModel.uiState.value)
    }

    @Test
    fun `showMessage 호출 시 ShowMessage effect 가 발행된다`() = runTest {
        val viewModel = FriendsViewModel()

        viewModel.uiEffect.test {
            viewModel.showMessage("준비 중입니다")
            assertEquals(FriendsUiEffect.ShowMessage("준비 중입니다"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
