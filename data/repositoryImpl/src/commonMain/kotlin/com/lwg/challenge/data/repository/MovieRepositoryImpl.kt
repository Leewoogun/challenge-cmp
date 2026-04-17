package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.Movie
import com.lwg.challenge.domain.repository.MovieRepository
import com.lwg.challenge.remote.api.MovieApi
import com.lwg.challenge.remote.mapper.toMovie
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [MovieRepository::class])
class MovieRepositoryImpl(
    private val movieApi: MovieApi,
) : MovieRepository {

    override fun getTopRatedMovies(page: Int, onError: (String) -> Unit): Flow<List<Movie>> = flow {
        movieApi.getTopRatedMovies(page)
            .suspendOnFailureWithErrorHandling(onError)
            .suspendOnSuccess {
                emit(response.results.map { it.toMovie() })
            }
    }
}
