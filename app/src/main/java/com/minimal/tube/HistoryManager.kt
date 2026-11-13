package com.minimal.tube

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object HistoryManager {
    private const val PREF_NAME = "minimal_tube_history"
    private const val KEY_HISTORY = "watch_history"
    private const val MAX_HISTORY = 50

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun addHistory(context: Context, item: VideoItem) {
        val list = getHistory(context).toMutableList()
        // Remove if duplicate URL exists so it bumps to the top
        list.removeAll { it.url == item.url }
        list.add(0, item)
        if (list.size > MAX_HISTORY) {
            list.removeAt(list.size - 1)
        }

        val jsonArray = JSONArray()
        for (v in list) {
            val obj = JSONObject().apply {
                put("url", v.url)
                put("title", v.title)
                put("uploaderName", v.uploaderName)
                put("viewCount", v.viewCount)
                put("uploadDate", v.uploadDate)
                put("thumbnailUrl", v.thumbnailUrl.orEmpty())
                put("durationText", v.durationText)
            }
            jsonArray.put(obj)
        }

        getPrefs(context).edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun getHistory(context: Context): List<VideoItem> {
        val jsonStr = getPrefs(context).getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<VideoItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    VideoItem(
                        url = obj.optString("url"),
                        title = obj.optString("title"),
                        uploaderName = obj.optString("uploaderName"),
                        viewCount = obj.optLong("viewCount"),
                        uploadDate = obj.optString("uploadDate"),
                        thumbnailUrl = obj.optString("thumbnailUrl").ifEmpty { null },
                        durationText = obj.optString("durationText")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearHistory(context: Context) {
        getPrefs(context).edit().remove(KEY_HISTORY).apply()
    }
}
