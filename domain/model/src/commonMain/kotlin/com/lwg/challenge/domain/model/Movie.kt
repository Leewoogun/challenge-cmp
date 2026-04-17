package com.lwg.challenge.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterUrl: String,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
)
