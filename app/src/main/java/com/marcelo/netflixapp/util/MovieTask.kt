package com.marcelo.netflixapp.util

import android.os.Handler
import android.os.Looper
import com.marcelo.netflixapp.model.Movie
import com.marcelo.netflixapp.model.MovieInfo
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieInfo: MovieInfo)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()

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

                if (statusCode == 400) {
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)

                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")

                    throw IOException(message)

                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream

                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                // json ja esta proton
                val movie = toMovieInfo(jsonAsString)

                handler.post {
                    // aqui roda dentro da UI-thread
                    callback.onResult(movie)
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

    private fun toMovieInfo(jsonAsString: String): MovieInfo {
        val json = JSONObject(jsonAsString)
        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val coverUrl = json.getString("cover_url")
        val similars = mutableListOf<Movie>()


        val jsonMovies = json.getJSONArray("movie")

        for (j in 0 until jsonMovies.length()) {
            val jsonMovie = jsonMovies.getJSONObject(j)
            val similarId = jsonMovie.getInt("id")
            val similarCoverUrl = jsonMovie.getString("cover_url")

            similars.add(Movie(id = similarId, coverUrl = similarCoverUrl))
        }

        return MovieInfo(Movie(id, coverUrl, title, desc, cast), similars)

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