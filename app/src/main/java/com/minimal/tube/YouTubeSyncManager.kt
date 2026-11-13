package com.minimal.tube

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ChannelItem(
    val channelId: String,
    val name: String,
    val avatarUrl: String,
    val subscriberCount: String = ""
)

object YouTubeSyncManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    private val YT_INITIAL_DATA_PATTERN = Pattern.compile("ytInitialData\\s*=\\s*(\\{.+?\\});<", Pattern.DOTALL)

    suspend fun fetchSubscriptionFeed(context: Context): List<VideoItem> = withContext(Dispatchers.IO) {
        val cookies = AccountManager.getCookies(context)
        if (cookies.isBlank()) return@withContext emptyList()

        try {
            val request = Request.Builder()
                .url("https://m.youtube.com/feed/subscriptions")
                .addHeader("User-Agent", MOBILE_USER_AGENT)
                .addHeader("Cookie", cookies)
                .addHeader("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string().orEmpty()

            val initialData = extractYtInitialData(html) ?: return@withContext emptyList()
            return@withContext parseVideosFromInitialData(initialData)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun fetchSubscribedChannels(context: Context): List<ChannelItem> = withContext(Dispatchers.IO) {
        val cookies = AccountManager.getCookies(context)
        if (cookies.isBlank()) return@withContext emptyList()

        try {
            val request = Request.Builder()
                .url("https://m.youtube.com/feed/channels")
                .addHeader("User-Agent", MOBILE_USER_AGENT)
                .addHeader("Cookie", cookies)
                .addHeader("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string().orEmpty()

            val initialData = extractYtInitialData(html) ?: return@withContext emptyList()
            return@withContext parseChannelsFromInitialData(initialData)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun fetchWatchHistory(context: Context): List<VideoItem> = withContext(Dispatchers.IO) {
        val cookies = AccountManager.getCookies(context)
        if (cookies.isBlank()) return@withContext emptyList()

        try {
            val request = Request.Builder()
                .url("https://m.youtube.com/feed/history")
                .addHeader("User-Agent", MOBILE_USER_AGENT)
                .addHeader("Cookie", cookies)
                .addHeader("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string().orEmpty()

            val initialData = extractYtInitialData(html) ?: return@withContext emptyList()
            return@withContext parseVideosFromInitialData(initialData)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    private fun extractYtInitialData(html: String): JSONObject? {
        val matcher = YT_INITIAL_DATA_PATTERN.matcher(html)
        if (matcher.find()) {
            val jsonStr = matcher.group(1)
            try {
                return JSONObject(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun parseVideosFromInitialData(root: JSONObject): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        findVideosRecursive(root, videos)
        return videos
    }

    private fun findVideosRecursive(obj: Any?, list: MutableList<VideoItem>) {
        when (obj) {
            is JSONObject -> {
                if (obj.has("compactVideoRenderer")) {
                    parseVideoRenderer(obj.getJSONObject("compactVideoRenderer"))?.let { list.add(it) }
                } else if (obj.has("videoWithContextRenderer")) {
                    parseVideoRenderer(obj.getJSONObject("videoWithContextRenderer"))?.let { list.add(it) }
                } else if (obj.has("videoRenderer")) {
                    parseVideoRenderer(obj.getJSONObject("videoRenderer"))?.let { list.add(it) }
                } else {
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        findVideosRecursive(obj.opt(key), list)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until obj.length()) {
                    findVideosRecursive(obj.opt(i), list)
                }
            }
        }
    }

    private fun parseVideoRenderer(renderer: JSONObject): VideoItem? {
        val videoId = renderer.optString("videoId")
        if (videoId.isEmpty()) return null

        val title = renderer.optJSONObject("headline")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("title")?.optString("simpleText")
            ?: "Video YouTube"

        val uploaderName = renderer.optJSONObject("shortBylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("bylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: ""

        var thumbUrl: String? = null
        val thumbs = renderer.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        if (thumbs != null && thumbs.length() > 0) {
            thumbUrl = thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
        }

        val durationText = renderer.optJSONObject("lengthText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("lengthText")?.optString("simpleText")
            ?: ""

        val uploadDate = renderer.optJSONObject("publishedTimeText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("publishedTimeText")?.optString("simpleText")
            ?: ""

        val url = "https://www.youtube.com/watch?v=$videoId"

        return VideoItem(
            url = url,
            title = title,
            uploaderName = uploaderName,
            viewCount = 0L,
            uploadDate = uploadDate,
            thumbnailUrl = thumbUrl,
            durationText = durationText
        )
    }

    private fun parseChannelsFromInitialData(root: JSONObject): List<ChannelItem> {
        val channels = mutableListOf<ChannelItem>()
        findChannelsRecursive(root, channels)
        return channels
    }

    private fun findChannelsRecursive(obj: Any?, list: MutableList<ChannelItem>) {
        when (obj) {
            is JSONObject -> {
                if (obj.has("channelListItemRenderer")) {
                    parseChannelRenderer(obj.getJSONObject("channelListItemRenderer"))?.let { list.add(it) }
                } else if (obj.has("channelRenderer")) {
                    parseChannelRenderer(obj.getJSONObject("channelRenderer"))?.let { list.add(it) }
                } else {
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        findChannelsRecursive(obj.opt(key), list)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until obj.length()) {
                    findChannelsRecursive(obj.opt(i), list)
                }
            }
        }
    }

    private fun parseChannelRenderer(renderer: JSONObject): ChannelItem? {
        val channelId = renderer.optString("channelId")
        if (channelId.isEmpty()) return null

        val name = renderer.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("title")?.optString("simpleText")
            ?: "Kênh"

        var avatarUrl = ""
        val thumbs = renderer.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        if (thumbs != null && thumbs.length() > 0) {
            avatarUrl = thumbs.optJSONObject(thumbs.length() - 1)?.optString("url").orEmpty()
        }

        val subscribers = renderer.optJSONObject("subscriberCountText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: renderer.optJSONObject("subscriberCountText")?.optString("simpleText")
            ?: ""

        return ChannelItem(
            channelId = channelId,
            name = name,
            avatarUrl = avatarUrl,
            subscriberCount = subscribers
        )
    }
}
