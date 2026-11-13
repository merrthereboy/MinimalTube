package com.minimal.tube

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class YouTubeLoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnClose: ImageView
    private lateinit var progressBar: ProgressBar

    private var loginCaptured = false

    companion object {
        const val CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
        const val LOGIN_URL = "https://accounts.google.com/ServiceLogin?service=youtube&continue=https%3A%2F%2Fm.youtube.com%2F"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        webView = findViewById(R.id.webViewLogin)
        btnClose = findViewById(R.id.btnCloseLogin)
        progressBar = findViewById(R.id.loginProgress)

        btnClose.setOnClickListener {
            finish()
        }

        setupWebView()
        webView.loadUrl(LOGIN_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString = CHROME_USER_AGENT
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                checkCookies(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                checkCookies(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }
    }

    private fun checkCookies(currentUrl: String?) {
        if (loginCaptured) return

        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie("https://m.youtube.com") 
            ?: cookieManager.getCookie(".youtube.com") 
            ?: ""

        // Khi người dùng đăng nhập thành công, YouTube sẽ cấp các cookie phiên xác thực: LOGIN_INFO, SAPISID, SID
        val hasLoginSession = cookies.contains("LOGIN_INFO") || 
                              (cookies.contains("SAPISID") && cookies.contains("SSID")) ||
                              cookies.contains("__Secure-1PSID")

        if (hasLoginSession && (currentUrl?.contains("youtube.com") == true)) {
            loginCaptured = true
            AccountManager.saveLoginSession(
                context = this,
                cookies = cookies,
                userName = "Tài khoản YouTube đã liên kết",
                userEmail = "Google Account"
            )
            Toast.makeText(this, "Đã đồng bộ tài khoản YouTube thành công!", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.clearHistory()
        super.onDestroy()
    }
}
