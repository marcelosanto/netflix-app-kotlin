package com.marcelo.netflixapp.model

data class MovieInfo(
    val movie: Movie,
    val similiars: List<Movie>
)
