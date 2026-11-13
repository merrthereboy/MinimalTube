package com.minimal.tube

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class MainActivity : AppCompatActivity() {

    private lateinit var tabHome: TextView
    private lateinit var tabTrending: TextView
    private lateinit var tabSearchToggle: TextView
    private lateinit var searchContainer: LinearLayout
    private lateinit var edtSearchQuery: EditText
    private lateinit var btnDoSearch: TextView

    private lateinit var playerSection: LinearLayout
    private lateinit var playerView: PlayerView
    private lateinit var txtCurrentPlayingTitle: TextView
    private lateinit var btnClosePlayer: TextView

    private lateinit var txtStatus: TextView
    private lateinit var recyclerViewVideos: RecyclerView
    private lateinit var videoAdapter: VideoAdapter

    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initExoPlayer()
        setupListeners()

        // Tải trang chủ mặc định lúc mở app
        loadTrendingVideos()
    }

    private fun initViews() {
        tabHome = findViewById(R.id.tabHome)
        tabTrending = findViewById(R.id.tabTrending)
        tabSearchToggle = findViewById(R.id.tabSearchToggle)
        searchContainer = findViewById(R.id.searchContainer)
        edtSearchQuery = findViewById(R.id.edtSearchQuery)
        btnDoSearch = findViewById(R.id.btnDoSearch)

        playerSection = findViewById(R.id.playerSection)
        playerView = findViewById(R.id.playerView)
        txtCurrentPlayingTitle = findViewById(R.id.txtCurrentPlayingTitle)
        btnClosePlayer = findViewById(R.id.btnClosePlayer)

        txtStatus = findViewById(R.id.txtStatus)
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos)

        videoAdapter = VideoAdapter(
            onPlayClick = { videoItem -> playVideo(videoItem) },
            onDownloadClick = { videoItem ->
                Toast.makeText(this, "Chuẩn bị tải: ${videoItem.title}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerViewVideos.layoutManager = LinearLayoutManager(this)
        recyclerViewVideos.adapter = videoAdapter
    }

    private fun initExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    private fun setupListeners() {
        tabHome.setOnClickListener {
            setActiveTab(tabHome)
            searchContainer.visibility = View.GONE
            loadTrendingVideos()
        }

        tabTrending.setOnClickListener {
            setActiveTab(tabTrending)
            searchContainer.visibility = View.GONE
            loadTrendingVideos()
        }

        tabSearchToggle.setOnClickListener {
            setActiveTab(tabSearchToggle)
            if (searchContainer.visibility == View.VISIBLE) {
                searchContainer.visibility = View.GONE
            } else {
                searchContainer.visibility = View.VISIBLE
                edtSearchQuery.requestFocus()
            }
        }

        btnDoSearch.setOnClickListener {
            val query = edtSearchQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                searchVideos(query)
            }
        }

        edtSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = edtSearchQuery.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchVideos(query)
                }
                true
            } else false
        }

        btnClosePlayer.setOnClickListener {
            exoPlayer?.stop()
            playerSection.visibility = View.GONE
        }
    }

    private fun setActiveTab(selectedTab: TextView) {
        val white = ContextCompat.getColor(this, R.color.text_white)
        val muted = ContextCompat.getColor(this, R.color.text_muted)

        tabHome.setTextColor(if (selectedTab == tabHome) white else muted)
        tabTrending.setTextColor(if (selectedTab == tabTrending) white else muted)
        tabSearchToggle.setTextColor(if (selectedTab == tabSearchToggle) white else muted)
    }

    private fun loadTrendingVideos() {
        showStatus("ĐANG TẢI XU HƯỚNG YOUTUBE...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val kiosk = service.kioskList.defaultKioskExtractor
                kiosk.fetchPage()

                val videoList = kiosk.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .map { item ->
                        VideoItem(
                            url = item.url,
                            title = item.name,
                            uploaderName = item.uploaderName.orEmpty(),
                            viewCount = item.viewCount,
                            uploadDate = item.textualUploadDate.orEmpty(),
                            thumbnailUrl = item.thumbnails.firstOrNull()?.url
                        )
                    }

                withContext(Dispatchers.Main) {
                    hideStatus()
                    videoAdapter.updateData(videoList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI KHI TẢI DỮ LIỆU: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun searchVideos(query: String) {
        showStatus("ĐANG TÌM KIẾM: $query")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val searchExtractor = service.getSearchExtractor(query)
                searchExtractor.fetchPage()

                val videoList = searchExtractor.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .map { item ->
                        VideoItem(
                            url = item.url,
                            title = item.name,
                            uploaderName = item.uploaderName.orEmpty(),
                            viewCount = item.viewCount,
                            uploadDate = item.textualUploadDate.orEmpty(),
                            thumbnailUrl = item.thumbnails.firstOrNull()?.url
                        )
                    }

                withContext(Dispatchers.Main) {
                    hideStatus()
                    videoAdapter.updateData(videoList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI TÌM KIẾM: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun playVideo(item: VideoItem) {
        txtCurrentPlayingTitle.text = item.title
        playerSection.visibility = View.VISIBLE
        showStatus("ĐANG GIẢI MÃ LUỒNG VIDEO KHÔNG QUẢNG CÁO...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val streamInfo = StreamInfo.getInfo(service, item.url)

                // Lấy luồng video chất lượng trực tiếp từ NewPipeExtractor
                val streamUrl = streamInfo.videoStreams.firstOrNull()?.content
                    ?: streamInfo.videoOnlyStreams.firstOrNull()?.content

                if (!streamUrl.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        hideStatus()
                        exoPlayer?.let { player ->
                            player.setMediaItem(MediaItem.fromUri(streamUrl))
                            player.prepare()
                            player.play()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showStatus("KHÔNG TÌM THẤY LUỒNG PHÁT HỢP LỆ.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI PHÁT LUỒNG: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showStatus(msg: String) {
        txtStatus.text = msg
        txtStatus.visibility = View.VISIBLE
    }

    private fun hideStatus() {
        txtStatus.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
