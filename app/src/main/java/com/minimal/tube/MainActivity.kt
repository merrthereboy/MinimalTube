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

        // Khởi động tải Trang chủ
        loadTrendingVideos(isTrendingTab = false)
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
            setActivePillTab(tabHome)
            searchContainer.visibility = View.GONE
            loadTrendingVideos(isTrendingTab = false)
        }

        tabTrending.setOnClickListener {
            setActivePillTab(tabTrending)
            searchContainer.visibility = View.GONE
            loadTrendingVideos(isTrendingTab = true)
        }

        tabSearchToggle.setOnClickListener {
            setActivePillTab(tabSearchToggle)
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

    private fun setActivePillTab(selectedTab: TextView) {
        val activeBg = ContextCompat.getDrawable(this, R.drawable.bg_pill_active)
        val inactiveBg = ContextCompat.getDrawable(this, R.drawable.bg_pill_inactive)
        val activeTextColor = ContextCompat.getColor(this, R.color.pill_active_text)
        val inactiveTextColor = ContextCompat.getColor(this, R.color.pill_inactive_text)

        val tabs = listOf(tabHome, tabTrending, tabSearchToggle)
        tabs.forEach { tab ->
            if (tab == selectedTab) {
                tab.background = activeBg
                tab.setTextColor(activeTextColor)
            } else {
                tab.background = inactiveBg
                tab.setTextColor(inactiveTextColor)
            }
        }
    }

    private fun loadTrendingVideos(isTrendingTab: Boolean) {
        showStatus(if (isTrendingTab) "ĐANG NẠP XU HƯỚNG MỚI NHẤT..." else "ĐANG NẠP DANH SÁCH ĐỀ XUẤT...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                
                // Thử tải qua Kiosk; nếu YouTube đổi cấu trúc thẻ, chuyển sang tìm kiếm thịnh hành làm fallback
                val videoList = try {
                    val kiosk = service.kioskList.defaultKioskExtractor
                    kiosk.fetchPage()
                    kiosk.initialPage.items.filterIsInstance<StreamInfoItem>().map { mapToItem(it) }
                } catch (e: Exception) {
                    val fallbackKeyword = if (isTrendingTab) "thịnh hành việt nam hôm nay" else "nhạc hot trending việt nam"
                    val search = service.getSearchExtractor(fallbackKeyword)
                    search.fetchPage()
                    search.initialPage.items.filterIsInstance<StreamInfoItem>().map { mapToItem(it) }
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
                    .map { mapToItem(it) }

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
        showStatus("ĐANG KẾT NỐI LUỒNG PHÁT SẠCH...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val streamInfo = StreamInfo.getInfo(service, item.url)

                // Tìm luồng phát phù hợp nhất (ưu tiên luồng tích hợp cả hình và tiếng, hoặc luồng tốt nhất)
                val videoStream = streamInfo.videoStreams
                    .filter { !it.isVideoOnly }
                    .maxByOrNull { it.resolution.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
                    ?: streamInfo.videoStreams.firstOrNull()
                    ?: streamInfo.videoOnlyStreams.firstOrNull()

                val streamUrl = videoStream?.content

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
                        showStatus("KHÔNG TÌM THẤY LUỒNG PHÁT TƯƠNG THÍCH.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI PHÁT LUỒNG: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun mapToItem(item: StreamInfoItem): VideoItem {
        return VideoItem(
            url = item.url,
            title = item.name,
            uploaderName = item.uploaderName.orEmpty(),
            viewCount = item.viewCount,
            uploadDate = item.textualUploadDate.orEmpty(),
            thumbnailUrl = item.thumbnails.firstOrNull()?.url
        )
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
