package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getTopRatedMovies(
        page: Int = 1,
        onError: (String) -> Unit,
    ): Flow<List<Movie>>
}
