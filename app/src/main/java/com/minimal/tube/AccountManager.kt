package com.minimal.tube

import android.content.Context
import android.content.SharedPreferences

object AccountManager {
    private const val PREF_NAME = "minimal_tube_account"
    private const val KEY_COOKIES = "youtube_cookies"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLoginSession(context: Context, cookies: String, userName: String = "", userEmail: String = "") {
        getPrefs(context).edit().apply {
            putString(KEY_COOKIES, cookies)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        DownloaderImpl.getInstance().userCookies = cookies
    }

    fun getCookies(context: Context): String {
        return getPrefs(context).getString(KEY_COOKIES, "").orEmpty()
    }

    fun getUserName(context: Context): String {
        return getPrefs(context).getString(KEY_USER_NAME, "").orEmpty()
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_USER_EMAIL, "").orEmpty()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false) && getCookies(context).isNotBlank()
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
        DownloaderImpl.getInstance().userCookies = ""
    }

    fun init(context: Context) {
        val cookies = getCookies(context)
        if (cookies.isNotBlank()) {
            DownloaderImpl.getInstance().userCookies = cookies
        }
    }
}
