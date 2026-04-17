package com.lwg.challenge.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class TopRatedMovieResponse(
    val page: Int,
    val results: List<MovieData> = emptyList(),
    val total_pages: Int = 0,
    val total_results: Int = 0,
)

@Serializable
data class MovieData(
    val id: Int,
    val title: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String = "",
    val release_date: String = "",
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val adult: Boolean = false,
    val backdrop_path: String = "",
    val genre_ids: List<Int> = emptyList(),
    val original_language: String = "",
    val video: Boolean = false,
)
