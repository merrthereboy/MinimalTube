package com.minimal.tube

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class ChannelActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var txtHeaderTitle: TextView
    private lateinit var imgBanner: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtSubCount: TextView
    private lateinit var btnSubscribe: TextView
    private lateinit var txtStatus: TextView
    private lateinit var recyclerViewVideos: RecyclerView
    private lateinit var videoAdapter: VideoAdapter

    private var isSubscribed = false

    companion object {
        const val EXTRA_CHANNEL_URL = "extra_channel_url"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_CHANNEL_AVATAR = "extra_channel_avatar"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        btnBack = findViewById(R.id.btnBackChannel)
        txtHeaderTitle = findViewById(R.id.txtHeaderChannelTitle)
        imgBanner = findViewById(R.id.imgChannelBanner)
        imgAvatar = findViewById(R.id.imgChannelBigAvatar)
        txtName = findViewById(R.id.txtChannelBigName)
        txtSubCount = findViewById(R.id.txtChannelSubCount)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        txtStatus = findViewById(R.id.txtChannelStatus)
        recyclerViewVideos = findViewById(R.id.recyclerViewChannelVideos)

        val channelUrl = intent.getStringExtra(EXTRA_CHANNEL_URL) ?: ""
        val initialName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "Kênh"
        val initialAvatar = intent.getStringExtra(EXTRA_CHANNEL_AVATAR) ?: ""

        txtName.text = initialName
        txtHeaderTitle.text = initialName
        if (initialAvatar.isNotEmpty()) {
            imgAvatar.load(initialAvatar) { crossfade(true) }
        }

        btnBack.setOnClickListener { finish() }

        btnSubscribe.setOnClickListener {
            isSubscribed = !isSubscribed
            if (isSubscribed) {
                btnSubscribe.text = "ĐÃ ĐĂNG KÝ"
                btnSubscribe.setBackgroundResource(R.drawable.bg_button_pill_secondary)
                Toast.makeText(this, "Đã đăng ký kênh", Toast.LENGTH_SHORT).show()
            } else {
                btnSubscribe.text = "ĐĂNG KÝ"
                btnSubscribe.setBackgroundResource(R.drawable.gradient_brand_button)
                Toast.makeText(this, "Đã hủy đăng ký", Toast.LENGTH_SHORT).show()
            }
        }

        videoAdapter = VideoAdapter(
            onPlayClick = { item ->
                finish()
            }
        )
        recyclerViewVideos.layoutManager = LinearLayoutManager(this)
        recyclerViewVideos.adapter = videoAdapter

        if (channelUrl.isNotEmpty()) {
            loadChannelData(channelUrl)
        } else {
            txtStatus.text = "Không tìm thấy đường dẫn kênh hợp lệ."
        }
    }

    private fun loadChannelData(url: String) {
        txtStatus.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val extractor = service.getChannelExtractor(url)
                extractor.fetchPage()

                val name = extractor.name
                val subCount = extractor.subscriberCount
                val avatar = extractor.avatarUrl
                val banner = extractor.bannerUrl
                val videos = extractor.initialPage.items.filterIsInstance<StreamInfoItem>().map { item ->
                    val durationFormatted = if (item.duration > 0) {
                        val minutes = item.duration / 60
                        val seconds = item.duration % 60
                        String.format("%02d:%02d", minutes, seconds)
                    } else ""

                    VideoItem(
                        url = item.url,
                        title = item.name,
                        uploaderName = name,
                        viewCount = item.viewCount,
                        uploadDate = item.textualUploadDate.orEmpty(),
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url,
                        durationText = durationFormatted
                    )
                }

                withContext(Dispatchers.Main) {
                    txtStatus.visibility = View.GONE
                    txtName.text = name
                    txtHeaderTitle.text = name
                    txtSubCount.text = subCount
                    if (!avatar.isNullOrEmpty()) {
                        imgAvatar.load(avatar) { crossfade(true) }
                    }
                    if (!banner.isNullOrEmpty()) {
                        imgBanner.load(banner) { crossfade(true) }
                        imgBanner.visibility = View.VISIBLE
                    }
                    videoAdapter.updateData(videos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    txtStatus.text = "Không thể nạp chi tiết kênh: ${e.localizedMessage}"
                }
            }
        }
    }
}
