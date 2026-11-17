package com.lwg.challenge.feature.challenge.create

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.CreateChallengeUseCase
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.feature.challenge.create.di.ChallengeCreateModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.koin.core.KoinApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.ksp.generated.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

// nullable @InjectedParam 이 KSP 생성 정의를 거쳐 런타임에 해석되는지 본다 — 기존 진입은 parametersOf(null) 에 의존한다.
@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeCreateViewModelKoinTest {

    private lateinit var fakeFriendsRepository: FakeFriendsRepository
    private lateinit var koinApp: KoinApplication

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeFriendsRepository = FakeFriendsRepository()
        koinApp = koinApplication {
            modules(
                ChallengeCreateModule().module,
                module {
                    single<FriendsRepository> { fakeFriendsRepository }
                    single { CreateChallengeUseCase(FakeChallengeRepository()) }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        koinApp.close()
        Dispatchers.resetMain()
    }

    private fun TestScope.subscribeUiState(viewModel: ChallengeCreateViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun sampleFriend(id: Long) = Friend(
        userId = id,
        nickname = "친구$id",
        profileImageUrl = null,
        since = LocalDateTime.parse("2026-01-01T00:00:00"),
    )

    @Test
    fun `생성된 모듈에서 parametersOf null 로 꺼내면 상대 선택 단계로 진입한다`() = runTest {
        fakeFriendsRepository.friends = listOf(sampleFriend(1), sampleFriend(7))

        val viewModel = koinApp.koin.get<ChallengeCreateViewModel> { parametersOf(null) }
        subscribeUiState(viewModel)

        assertIs<ChallengeCreateUiState.FriendPick>(viewModel.uiState.value)
    }

    @Test
    fun `생성된 모듈에서 상대 id 로 꺼내면 그 상대가 선택된 미션 입력 단계로 진입한다`() = runTest {
        fakeFriendsRepository.friends = listOf(sampleFriend(1), sampleFriend(7))

        val viewModel = koinApp.koin.get<ChallengeCreateViewModel> { parametersOf(7L) }
        subscribeUiState(viewModel)

        assertIs<ChallengeCreateUiState.MissionInput>(viewModel.uiState.value)
    }
}
