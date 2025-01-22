package com.example.cpaas_telemo_flashcall_sdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MissedCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_IDLE && !incomingNumber.isNullOrEmpty()) {
            Log.d("MissedCallReceiver", "Missed call from: $incomingNumber")

            // Manually instantiate dependencies
            context?.let { ctx ->
                val preferencesManager = PreferencesManager(ctx)
                val apiService = RetrofitClient.apiService
                val repository = FlashRepository(apiService)
                val callback = FlashSDK.getInstance().getCallback()
                val otpIndex = preferencesManager.getindex()
                val id = preferencesManager.getid()
                val appId = preferencesManager.getAppId()

                processMissedCall(ctx, incomingNumber, id, otpIndex, callback, appId, repository)
            }
        }
    }


    /**
     * Process the missed call and verify with the server
     */
    private fun processMissedCall(
        context: Context,
        incomingNumber: String,
        id: String?,
        otpIndex: String?,
        callback: FlashCallback?,
        appId: String?,
        repository: FlashRepository
    ) {
        // Sanitize and process the incoming number
        val sanitizedNumber = incomingNumber.replace("+", "")

        val finalCombinedNumber = buildCombinedNumber(sanitizedNumber, otpIndex)

        // Log the details

        // Perform verification with the API
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = repository.verifyStatus(
                    callId = id.orEmpty(),
                    otp = finalCombinedNumber.orEmpty(),
                    appId = appId.orEmpty()
                )

                if (response.status == 200) {
                    Log.d("MissedCallReceiver", "Verification successful!")
                    callback?.onSuccess("Verification successful!")
                } else {
                    Log.e("MissedCallReceiver", "Verification failed: ${response.verification_status}")
                    callback?.onFailure("Verification failed: ${response.verification_status}")
                }
            } catch (e: Exception) {
                Log.e("MissedCallReceiver", "Verification error: ${e.message}")
                callback?.onFailure("Verification failed: ${e.message}")
            }
        }
    }

    /**
     * Build the combined number using the OTP index
     */
    private fun buildCombinedNumber(sanitizedNumber: String, otpIndex: String?): String {
        var finalcombinedNumber: String? = null

        if (!otpIndex.isNullOrEmpty()) {
            val indices = otpIndex.split(",").map { it.trim().toInt() }
            val combinedNumber = StringBuilder()

            for (index in indices) {
                if (index < sanitizedNumber.length) {
                    combinedNumber.append(sanitizedNumber[index])
                }
                finalcombinedNumber = combinedNumber.toString()

            }
            // Use combined number and ID together (e.g., send to API)
        } else {
            val fallbackNumber = sanitizedNumber.takeLast(5)
            finalcombinedNumber = fallbackNumber

        }
        return finalcombinedNumber.toString()
    }
}