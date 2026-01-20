package com.judev.udhaarbook

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val USER_EMAIL = "user_email"
        private const val USER_PASSWORD = "user_password"
        private const val USER_NAME = "user_name"
        private const val USER_IMAGE = "user_image"
        private const val APP_THEME = "app_theme" // "system", "light", "dark"
    }

    fun saveUser(email: String, password: String) {
        val editor = prefs.edit()
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_PASSWORD, password)
        if (prefs.getString(USER_NAME, null) == null) {
            editor.putString(USER_NAME, email.split("@")[0])
        }
        editor.apply()
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(IS_LOGGED_IN, false)

    fun getUserEmail(): String? = prefs.getString(USER_EMAIL, null)
    fun getUserPassword(): String? = prefs.getString(USER_PASSWORD, null)
    
    fun getUserName(): String = prefs.getString(USER_NAME, "") ?: ""
    fun saveUserName(name: String) {
        prefs.edit().putString(USER_NAME, name).apply()
    }

    fun getUserImage(): String? = prefs.getString(USER_IMAGE, null)
    fun saveUserImage(path: String) {
        prefs.edit().putString(USER_IMAGE, path).apply()
    }

    fun getTheme(): String = prefs.getString(APP_THEME, "system") ?: "system"
    fun saveTheme(theme: String) {
        prefs.edit().putString(APP_THEME, theme).apply()
    }

    fun logout() {
        // Keep theme preference on logout, or clear it if you want total reset
        val theme = getTheme()
        prefs.edit().clear().apply()
        saveTheme(theme)
    }
}
