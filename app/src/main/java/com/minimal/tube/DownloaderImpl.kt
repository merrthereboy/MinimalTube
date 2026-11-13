package com.minimal.tube

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownloaderImpl private constructor() : Downloader() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = okhttp3.Request.Builder()
            .url(url)

        headers?.forEach { (name, values) ->
            values.forEach { value ->
                requestBuilder.addHeader(name, value)
            }
        }

        val requestBody = dataToSend?.toRequestBody()

        when (httpMethod.uppercase()) {
            "GET" -> requestBuilder.get()
            "HEAD" -> requestBuilder.head()
            "POST" -> requestBuilder.post(requestBody ?: ByteArray(0).toRequestBody())
            else -> requestBuilder.method(httpMethod, requestBody)
        }

        val okResponse = client.newCall(requestBuilder.build()).execute()
        val responseBody = okResponse.body?.string().orEmpty()
        val responseHeaders = okResponse.headers.toMultimap()

        return Response(
            okResponse.code,
            okResponse.message,
            responseHeaders,
            responseBody,
            okResponse.request.url.toString()
        )
    }

    companion object {
        private var instance: DownloaderImpl? = null

        fun getInstance(): DownloaderImpl {
            if (instance == null) {
                instance = DownloaderImpl()
            }
            return instance!!
        }
    }
}
