package com.example.cpaas_telemo_flashcall_sdk

import com.example.cpaas_telemo_flashcall_sdk.ApiResponse
import com.example.cpaas_telemo_flashcall_sdk.ApiService
import com.example.cpaas_telemo_flashcall_sdk.VerifyRequest
import com.example.cpaas_telemo_flashcall_sdk.VerifyRequestAfterCall
import com.google.gson.JsonObject
import javax.inject.Inject

class FlashRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun startVerification(phoneNumber: String, appId: String): JsonObject {
        return apiService.startVerification(appId, VerifyRequest(phoneNumber, "In"))
    }

    suspend fun verifyStatus(callId: String, otp: String, appId: String): ApiResponse {
        return apiService.verifyStatus(appId, VerifyRequestAfterCall(callId, otp))
    }
}