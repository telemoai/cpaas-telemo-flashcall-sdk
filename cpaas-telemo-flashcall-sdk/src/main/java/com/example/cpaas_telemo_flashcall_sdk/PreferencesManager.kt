package com.example.cpaas_telemo_flashcall_sdk

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun saveCredentials(token: String, baseUrl: String, appId: String) {
        sharedPreferences.edit().apply {
            putString("Base_url", baseUrl)
            putString("token", token)
            putString("app_id", appId)
            apply()
        }
    }

    fun saveOtpIndex(otpIndex: String, id: String) {
        sharedPreferences.edit().apply {
            putString("otp_index", otpIndex)
            putString("id", id)
            apply()
        }
    }

    fun getToken(): String? = sharedPreferences.getString("token", null)
    fun getBaseUrl(): String? = sharedPreferences.getString("Base_url", null)
    fun getAppId(): String? = sharedPreferences.getString("app_id", null)
    fun getid(): String? = sharedPreferences.getString("id", null)
    fun getindex(): String? = sharedPreferences.getString("otp_index", null)
}