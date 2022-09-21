package com.marcelo.netflixapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marcelo.netflixapp.model.Category
import com.marcelo.netflixapp.model.Movie

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categories = mutableListOf<Category>()
        for (j in 0 until 10) {
            val movies = mutableListOf<Movie>()
            for (i in 0 until 10) {
                val movie = if (i % 2 == 0) Movie(R.drawable.movie) else Movie(R.drawable.movie_4)
                movies.add(movie)
            }
            val category = Category("Cat $j", movies)
            categories.add(category)
        }


        val adapter = CategoryAdapter(categories)
        val rv: RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }


}