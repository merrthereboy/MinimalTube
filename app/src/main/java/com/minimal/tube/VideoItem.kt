package com.minimal.tube

data class VideoItem(
    val url: String,
    val title: String,
    val uploaderName: String,
    val viewCount: Long,
    val uploadDate: String,
    val thumbnailUrl: String?,
    val durationText: String = ""
)
