package com.minimal.tube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class MainActivity : AppCompatActivity() {

    // Top Bar & Search
    private lateinit var btnToggleSearch: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var edtSearchQuery: EditText
    private lateinit var btnDoSearch: TextView
    private lateinit var chipsScrollView: HorizontalScrollView

    // Chips
    private lateinit var chipAll: TextView
    private lateinit var chipLive: TextView
    private lateinit var chipMusic: TextView
    private lateinit var chipGaming: TextView
    private lateinit var chipNews: TextView
    private lateinit var chipTech: TextView
    private lateinit var chipTrending: TextView

    // Tab Containers
    private lateinit var tabContentHome: LinearLayout
    private lateinit var tabContentShorts: LinearLayout
    private lateinit var tabContentSubscriptions: LinearLayout
    private lateinit var tabContentLibrary: ScrollView
    private lateinit var tabContentProfile: ScrollView

    // Bottom Navigation Items
    private lateinit var navHome: LinearLayout
    private lateinit var navShorts: LinearLayout
    private lateinit var navSubs: LinearLayout
    private lateinit var navLibrary: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var iconNavHome: ImageView
    private lateinit var txtNavHome: TextView
    private lateinit var iconNavShorts: ImageView
    private lateinit var txtNavShorts: TextView
    private lateinit var iconNavSubs: ImageView
    private lateinit var txtNavSubs: TextView
    private lateinit var iconNavLibrary: ImageView
    private lateinit var txtNavLibrary: TextView
    private lateinit var iconNavProfile: ImageView
    private lateinit var txtNavProfile: TextView

    // RecyclerViews & Adapters
    private lateinit var recyclerViewHome: RecyclerView
    private lateinit var recyclerViewShorts: RecyclerView
    private lateinit var recyclerViewSubs: RecyclerView
    private lateinit var recyclerViewHistory: RecyclerView

    private lateinit var homeAdapter: VideoAdapter
    private lateinit var shortsAdapter: VideoAdapter
    private lateinit var subsAdapter: VideoAdapter
    private lateinit var historyAdapter: VideoAdapter

    // Fans Zone (Subscriptions) views
    private lateinit var cardSubsNotLoggedIn: LinearLayout
    private lateinit var btnSubsLogin: LinearLayout

    // Library Views
    private lateinit var btnClearHistory: TextView
    private lateinit var txtEmptyHistory: TextView

    // Profile Views
    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileStatus: TextView
    private lateinit var btnProfileGoogleLogin: LinearLayout
    private lateinit var txtProfileLoginAction: TextView

    // Player Section
    private lateinit var playerSection: LinearLayout
    private lateinit var playerView: PlayerView
    private lateinit var txtCurrentPlayingTitle: TextView
    private lateinit var btnClosePlayer: ImageView
    private var exoPlayer: ExoPlayer? = null

    // Status / Message
    private lateinit var txtStatus: TextView

    // Current active tab index (0: Home, 1: Shorts, 2: Subs, 3: Library, 4: Profile)
    private var currentTabIndex = 0

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            updateAccountUI()
            if (currentTabIndex == 2) {
                loadSubscriptionsFeed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AccountManager.init(this)

        initViews()
        initExoPlayer()
        setupBottomNavigation()
        setupChips()
        setupSearch()
        updateAccountUI()

        // Khởi động tải dữ liệu Trang chủ
        loadHomeFeed("tất cả")
    }

    private fun initViews() {
        btnToggleSearch = findViewById(R.id.btnToggleSearch)
        searchContainer = findViewById(R.id.searchContainer)
        edtSearchQuery = findViewById(R.id.edtSearchQuery)
        btnDoSearch = findViewById(R.id.btnDoSearch)
        chipsScrollView = findViewById(R.id.chipsScrollView)

        chipAll = findViewById(R.id.chipAll)
        chipLive = findViewById(R.id.chipLive)
        chipMusic = findViewById(R.id.chipMusic)
        chipGaming = findViewById(R.id.chipGaming)
        chipNews = findViewById(R.id.chipNews)
        chipTech = findViewById(R.id.chipTech)
        chipTrending = findViewById(R.id.chipTrending)

        tabContentHome = findViewById(R.id.tabContentHome)
        tabContentShorts = findViewById(R.id.tabContentShorts)
        tabContentSubscriptions = findViewById(R.id.tabContentSubscriptions)
        tabContentLibrary = findViewById(R.id.tabContentLibrary)
        tabContentProfile = findViewById(R.id.tabContentProfile)

        navHome = findViewById(R.id.navHome)
        navShorts = findViewById(R.id.navShorts)
        navSubs = findViewById(R.id.navSubs)
        navLibrary = findViewById(R.id.navLibrary)
        navProfile = findViewById(R.id.navProfile)

        iconNavHome = findViewById(R.id.iconNavHome)
        txtNavHome = findViewById(R.id.txtNavHome)
        iconNavShorts = findViewById(R.id.iconNavShorts)
        txtNavShorts = findViewById(R.id.txtNavShorts)
        iconNavSubs = findViewById(R.id.iconNavSubs)
        txtNavSubs = findViewById(R.id.txtNavSubs)
        iconNavLibrary = findViewById(R.id.iconNavLibrary)
        txtNavLibrary = findViewById(R.id.txtNavLibrary)
        iconNavProfile = findViewById(R.id.iconNavProfile)
        txtNavProfile = findViewById(R.id.txtNavProfile)

        recyclerViewHome = findViewById(R.id.recyclerViewHome)
        recyclerViewShorts = findViewById(R.id.recyclerViewShorts)
        recyclerViewSubs = findViewById(R.id.recyclerViewSubs)
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)

        cardSubsNotLoggedIn = findViewById(R.id.cardSubsNotLoggedIn)
        btnSubsLogin = findViewById(R.id.btnSubsLogin)

        btnClearHistory = findViewById(R.id.btnClearHistory)
        txtEmptyHistory = findViewById(R.id.txtEmptyHistory)

        txtProfileName = findViewById(R.id.txtProfileName)
        txtProfileStatus = findViewById(R.id.txtProfileStatus)
        btnProfileGoogleLogin = findViewById(R.id.btnProfileGoogleLogin)
        txtProfileLoginAction = findViewById(R.id.txtProfileLoginAction)

        playerSection = findViewById(R.id.playerSection)
        playerView = findViewById(R.id.playerView)
        txtCurrentPlayingTitle = findViewById(R.id.txtCurrentPlayingTitle)
        btnClosePlayer = findViewById(R.id.btnClosePlayer)
        txtStatus = findViewById(R.id.txtStatus)

        // Setup Adapters
        homeAdapter = VideoAdapter(
            onPlayClick = { playVideo(it) },
            onDownloadClick = { Toast.makeText(this, "Tính năng tải về: ${it.title}", Toast.LENGTH_SHORT).show() }
        )
        recyclerViewHome.layoutManager = LinearLayoutManager(this)
        recyclerViewHome.adapter = homeAdapter

        shortsAdapter = VideoAdapter(
            onPlayClick = { playVideo(it) }
        )
        recyclerViewShorts.layoutManager = LinearLayoutManager(this)
        recyclerViewShorts.adapter = shortsAdapter

        subsAdapter = VideoAdapter(
            onPlayClick = { playVideo(it) }
        )
        recyclerViewSubs.layoutManager = LinearLayoutManager(this)
        recyclerViewSubs.adapter = subsAdapter

        historyAdapter = VideoAdapter(
            onPlayClick = { playVideo(it) }
        )
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        recyclerViewHistory.adapter = historyAdapter

        // Player Close
        btnClosePlayer.setOnClickListener {
            exoPlayer?.stop()
            playerSection.visibility = View.GONE
        }

        // Subscriptions Login CTA
        btnSubsLogin.setOnClickListener {
            openLoginActivity()
        }

        // History Clear
        btnClearHistory.setOnClickListener {
            HistoryManager.clearHistory(this)
            loadHistoryList()
            Toast.makeText(this, "Đã xóa toàn bộ lịch sử xem", Toast.LENGTH_SHORT).show()
        }

        // Profile Login / Logout button
        btnProfileGoogleLogin.setOnClickListener {
            if (AccountManager.isLoggedIn(this)) {
                AccountManager.logout(this)
                updateAccountUI()
                Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            } else {
                openLoginActivity()
            }
        }
    }

    private fun initExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    private fun setupBottomNavigation() {
        navHome.setOnClickListener { selectTab(0) }
        navShorts.setOnClickListener { selectTab(1) }
        navSubs.setOnClickListener { selectTab(2) }
        navLibrary.setOnClickListener { selectTab(3) }
        navProfile.setOnClickListener { selectTab(4) }
    }

    private fun selectTab(tabIndex: Int) {
        currentTabIndex = tabIndex
        hideKeyboard()

        // Toggle Content Visibility
        tabContentHome.visibility = if (tabIndex == 0) View.VISIBLE else View.GONE
        tabContentShorts.visibility = if (tabIndex == 1) View.VISIBLE else View.GONE
        tabContentSubscriptions.visibility = if (tabIndex == 2) View.VISIBLE else View.GONE
        tabContentLibrary.visibility = if (tabIndex == 3) View.VISIBLE else View.GONE
        tabContentProfile.visibility = if (tabIndex == 4) View.VISIBLE else View.GONE

        // Chips only visible on Home tab
        chipsScrollView.visibility = if (tabIndex == 0) View.VISIBLE else View.GONE

        // Update Bottom Nav Colors (Active: Cyan #00F2FE, Inactive: #71717A)
        val activeColor = ContextCompat.getColor(this, R.color.active_accent)
        val inactiveColor = ContextCompat.getColor(this, R.color.inactive_nav)

        val navItems = listOf(
            Triple(iconNavHome, txtNavHome, 0),
            Triple(iconNavShorts, txtNavShorts, 1),
            Triple(iconNavSubs, txtNavSubs, 2),
            Triple(iconNavLibrary, txtNavLibrary, 3),
            Triple(iconNavProfile, txtNavProfile, 4)
        )

        navItems.forEach { (icon, text, index) ->
            if (index == tabIndex) {
                icon.setColorFilter(activeColor)
                text.setTextColor(activeColor)
                text.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                icon.setColorFilter(inactiveColor)
                text.setTextColor(inactiveColor)
                text.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }

        // Lazy load tab data
        when (tabIndex) {
            1 -> loadShortsFeed()
            2 -> loadSubscriptionsFeed()
            3 -> loadHistoryList()
            4 -> updateAccountUI()
        }
    }

    private fun setupChips() {
        val chips = listOf(
            Pair(chipAll, "tất cả"),
            Pair(chipLive, "trực tiếp việt nam"),
            Pair(chipMusic, "nhạc thịnh hành việt nam"),
            Pair(chipGaming, "gaming việt nam"),
            Pair(chipNews, "tin tức việt nam hôm nay"),
            Pair(chipTech, "công nghệ"),
            Pair(chipTrending, "thịnh hành")
        )

        chips.forEach { (chipView, keyword) ->
            chipView.setOnClickListener {
                setActiveChip(chipView)
                loadHomeFeed(keyword)
            }
        }
    }

    private fun setActiveChip(selectedChip: TextView) {
        val activeBg = ContextCompat.getDrawable(this, R.drawable.bg_pill_chip_active)
        val inactiveBg = ContextCompat.getDrawable(this, R.drawable.bg_pill_chip_inactive)
        val activeText = ContextCompat.getColor(this, R.color.pill_active_text)
        val inactiveText = ContextCompat.getColor(this, R.color.pill_inactive_text)

        val allChips = listOf(chipAll, chipLive, chipMusic, chipGaming, chipNews, chipTech, chipTrending)
        allChips.forEach { chip ->
            if (chip == selectedChip) {
                chip.background = activeBg
                chip.setTextColor(activeText)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.background = inactiveBg
                chip.setTextColor(inactiveText)
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun setupSearch() {
        btnToggleSearch.setOnClickListener {
            if (searchContainer.visibility == View.VISIBLE) {
                searchContainer.visibility = View.GONE
                hideKeyboard()
            } else {
                searchContainer.visibility = View.VISIBLE
                edtSearchQuery.requestFocus()
                showKeyboard()
            }
        }

        btnDoSearch.setOnClickListener {
            val query = edtSearchQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                hideKeyboard()
                selectTab(0)
                loadSearchResults(query)
            }
        }

        edtSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = edtSearchQuery.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard()
                    selectTab(0)
                    loadSearchResults(query)
                }
                true
            } else false
        }
    }

    private fun loadHomeFeed(keyword: String) {
        showStatus("ĐANG TẢI DỮ LIỆU ĐỀ XUẤT...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val videoList: List<VideoItem> = if (keyword == "tất cả") {
                    try {
                        val kiosk = service.kioskList.defaultKioskExtractor
                        kiosk.fetchPage()
                        kiosk.initialPage.items.filterIsInstance<StreamInfoItem>().map { mapToItem(it) }
                    } catch (e: Exception) {
                        val search = service.getSearchExtractor("video hot thịnh hành việt nam")
                        search.fetchPage()
                        search.initialPage.items.filterIsInstance<StreamInfoItem>().map { mapToItem(it) }
                    }
                } else {
                    val search = service.getSearchExtractor(keyword)
                    search.fetchPage()
                    search.initialPage.items.filterIsInstance<StreamInfoItem>().map { mapToItem(it) }
                }

                withContext(Dispatchers.Main) {
                    hideStatus()
                    homeAdapter.updateData(videoList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI KHI TẢI DỮ LIỆU: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun loadSearchResults(query: String) {
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
                    homeAdapter.updateData(videoList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI TÌM KIẾM: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun loadShortsFeed() {
        showStatus("ĐANG NẠP SHORTS THỊNH HÀNH...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val search = service.getSearchExtractor("#shorts việt nam")
                search.fetchPage()
                val list = search.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .map { mapToItem(it) }

                withContext(Dispatchers.Main) {
                    hideStatus()
                    shortsAdapter.updateData(list)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("LỖI NẠP SHORTS: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun loadSubscriptionsFeed() {
        if (!AccountManager.isLoggedIn(this)) {
            cardSubsNotLoggedIn.visibility = View.VISIBLE
            recyclerViewSubs.visibility = View.GONE
            hideStatus()
            return
        }

        cardSubsNotLoggedIn.visibility = View.GONE
        recyclerViewSubs.visibility = View.VISIBLE
        showStatus("ĐANG ĐỒNG BỘ KÊNH ĐĂNG KÝ...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                // Sử dụng subscription feed với cookie đã nạp trong DownloaderImpl
                val subExtractor = service.getFeedExtractor("https://www.youtube.com/feed/subscriptions")
                subExtractor.fetchPage()
                val list = subExtractor.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .map { mapToItem(it) }

                withContext(Dispatchers.Main) {
                    hideStatus()
                    subsAdapter.updateData(list)
                }
            } catch (e: Exception) {
                // Fallback nếu YouTube cấu trúc feed subscriptions đổi
                withContext(Dispatchers.Main) {
                    hideStatus()
                    Toast.makeText(this@MainActivity, "Đã kết nối tài khoản. Đang tải video đề xuất...", Toast.LENGTH_SHORT).show()
                    loadHomeFeed("tất cả")
                }
            }
        }
    }

    private fun loadHistoryList() {
        val historyList = HistoryManager.getHistory(this)
        if (historyList.isEmpty()) {
            txtEmptyHistory.visibility = View.VISIBLE
            recyclerViewHistory.visibility = View.GONE
        } else {
            txtEmptyHistory.visibility = View.GONE
            recyclerViewHistory.visibility = View.VISIBLE
            historyAdapter.updateData(historyList)
        }
    }

    private fun updateAccountUI() {
        val isLoggedIn = AccountManager.isLoggedIn(this)
        if (isLoggedIn) {
            txtProfileName.text = "Tài khoản YouTube đã liên kết"
            txtProfileStatus.text = "ĐÃ ĐỒNG BỘ • Kênh đăng ký & Lịch sử"
            txtProfileStatus.setTextColor(ContextCompat.getColor(this, R.color.active_accent))
            txtProfileLoginAction.text = "ĐĂNG XUẤT TÀI KHOẢN"
        } else {
            txtProfileName.text = "Khách (Chưa đồng bộ)"
            txtProfileStatus.text = "Chưa kết nối tài khoản Google"
            txtProfileStatus.setTextColor(ContextCompat.getColor(this, R.color.text_sub))
            txtProfileLoginAction.text = "ĐỒNG BỘ TÀI KHOẢN GOOGLE"
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(this, YouTubeLoginActivity::class.java)
        loginLauncher.launch(intent)
    }

    private fun playVideo(item: VideoItem) {
        hideKeyboard()
        HistoryManager.addHistory(this, item)
        txtCurrentPlayingTitle.text = item.title
        playerSection.visibility = View.VISIBLE
        showStatus("ĐANG KẾT NỐI LUỒNG PHÁT SẠCH...")

        lifecycleScope.launch(Dispatchers.IO) {
            var streamUrl: String? = null
            try {
                val service = ServiceList.YouTube
                val streamInfo = try {
                    StreamInfo.getInfo(service, item.url)
                } catch (e: Exception) {
                    // Retry một lần nếu gặp reload glitch
                    StreamInfo.getInfo(service, item.url)
                }

                // 1. Luồng HLS thích ứng đa độ phân giải
                val hlsUrl = streamInfo.hlsUrl
                if (!hlsUrl.isNullOrEmpty()) {
                    streamUrl = hlsUrl
                } else {
                    // 2. Luồng MP4 progressive (Video + Audio)
                    val combined = streamInfo.videoStreams
                        .filter { !it.isVideoOnly }
                        .maxByOrNull { it.resolution.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }

                    streamUrl = combined?.content
                        ?: streamInfo.videoStreams.firstOrNull()?.content
                        ?: streamInfo.videoOnlyStreams.firstOrNull()?.content
                }

                withContext(Dispatchers.Main) {
                    if (!streamUrl.isNullOrEmpty()) {
                        hideStatus()
                        exoPlayer?.let { player ->
                            player.setMediaItem(MediaItem.fromUri(streamUrl))
                            player.prepare()
                            player.play()
                        }
                    } else {
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
        val durationFormatted = if (item.duration > 0) {
            val minutes = item.duration / 60
            val seconds = item.duration % 60
            String.format("%02d:%02d", minutes, seconds)
        } else ""

        return VideoItem(
            url = item.url,
            title = item.name,
            uploaderName = item.uploaderName.orEmpty(),
            viewCount = item.viewCount,
            uploadDate = item.textualUploadDate.orEmpty(),
            thumbnailUrl = item.thumbnails.firstOrNull()?.url,
            durationText = durationFormatted
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken ?: window.decorView.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(edtSearchQuery, InputMethodManager.SHOW_IMPLICIT)
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
