package com.example.cpaas_telemo_flashcall_sdk

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class FlashSDK private constructor() {

    private var flashCallback: FlashCallback? = null
    private var preferencesManager: PreferencesManager? = null

    companion object {
        private var instance: FlashSDK? = null

        /**
         * Get the singleton instance of FlashSDK.
         */
        fun getInstance(): FlashSDK {
            if (instance == null) {
                instance = FlashSDK()
            }
            return instance!!
        }
    }

    fun getCallback() = flashCallback


    /**
     * Initialize the SDK with required parameters.
     * This should be called once before using other SDK methods.
     */
    fun initialize(
        context: Context,
        baseUrl: String,
        token: String,
        appId: String,
        callback: FlashCallback
    ) {
        // Save callback
        flashCallback = callback

        // Initialize PreferencesManager
        preferencesManager = PreferencesManager(context)

        // Configure RetrofitClient
        RetrofitClient.setBaseUrl(baseUrl)
        RetrofitClient.setToken(token)

        // Save credentials in PreferencesManager
        preferencesManager?.saveCredentials(token, baseUrl, appId)

        // Validate input parameters
        if (appId.isEmpty() || token.isEmpty()) {
            flashCallback?.onFailure("SDK initialization failed: Missing appId or token.")
        }
    }

    /**
     * Start the phone number verification process.
     */
    fun startVerification(phoneNumber: String, name: String) {
        val appId = preferencesManager?.getAppId()
        if (appId.isNullOrEmpty()) {
            flashCallback?.onFailure("SDK is not properly initialized: appId is missing.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.startVerification(
                    appId = appId,
                    request = VerifyRequest(phoneNumber, name)
                )
                processDynamicResponse(response)
            } catch (e: Exception) {
                flashCallback?.onFailure("Verification failed: ${e.message}")
            }
        }
    }

    /**
     * Process the API response for verification.
     */
    private fun processDynamicResponse(response: JsonObject) {
        val status = response.get("status").asInt
        if (status == 200) {
            val id = response.get("id").asString
            val otpIndex = response.get("otp_index")?.asString

            preferencesManager?.saveId(id)

            otpIndex?.let {
                preferencesManager?.saveOtpIndex(it)
               // flashCallback?.onSuccess("OTP Index: $it")
            }
        } else {
            val errorMessage = response.get("error_message")?.asString ?: "Unknown error"
            flashCallback?.onFailure("Verification failed: $errorMessage")
        }
    }
}