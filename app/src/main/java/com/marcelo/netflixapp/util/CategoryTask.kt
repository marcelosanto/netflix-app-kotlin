package com.marcelo.netflixapp.util

import android.os.Handler
import android.os.Looper
import com.marcelo.netflixapp.model.Category
import com.marcelo.netflixapp.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        // estamos utilizando a UI-thread
        val executor = Executors.newSingleThreadExecutor()

        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null
            var buffer: BufferedInputStream? = null

            try {
                // novo processo paralelo - nova thread
                val requestUrl = URL(url) // abrir uma url
                urlConnection =
                    requestUrl.openConnection() as HttpsURLConnection // abrir uma conexao
                urlConnection.readTimeout = 2000 // tempo de leitura da conexao
                urlConnection.connectTimeout = 2000 // tempo de conexao

                val statusCode: Int = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream

                //1º forma - simples e rapida
                //val jsonAsString = stream.bufferedReader().use { it.readText() } // bytes -> string


                //2º forma
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                // json ja esta proton
                val categories = toCategories(jsonAsString)

                handler.post {
                    // aqui roda dentro da UI-thread
                    callback.onResult(categories)
                }


            } catch (e: IOException) {
                handler.post { callback.onFailure(e.message.toString()) }
            } finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }

        }
    }

    private fun toCategories(jsonAsString: String): List<Category> {
        val categories = mutableListOf<Category>()

        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")
        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)

            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")

            val movies = mutableListOf<Movie>()

            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                movies.add(Movie(id, coverUrl))
            }
            categories.add(Category(title, movies))
        }

        return categories
    }

    private fun toString(stream: InputStream): String {
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read: Int

        while (true) {
            read = stream.read(bytes)
            if (read <= 0) break

            baos.write(bytes, 0, read)
        }

        return String(baos.toByteArray())
    }
}