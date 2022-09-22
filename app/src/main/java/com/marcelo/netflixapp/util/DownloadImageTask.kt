package com.marcelo.netflixapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class DownloadImageTask(private val callback: DownloadImageTask.Callback) {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onResult(bitmap: Bitmap)
    }

    fun execute(url: String) {
        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestUrl = URL(url)
                urlConnection =
                    requestUrl.openConnection() as HttpsURLConnection // abrir uma conexao
                urlConnection.readTimeout = 2000 // tempo de leitura da conexao
                urlConnection.connectTimeout = 2000 // tempo de conexao

                val statusCode: Int = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream
                val bitmap = BitmapFactory.decodeStream(stream)

                handler.post {
                    callback.onResult(bitmap)
                }

            } catch (e: IOException) {
                Log.e("TAG", "execute: ${e.message}", e)
            } finally {
                urlConnection?.disconnect()
                stream?.close()
            }
        }
    }

}