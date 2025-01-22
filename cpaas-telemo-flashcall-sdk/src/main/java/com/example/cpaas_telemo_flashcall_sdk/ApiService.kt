package com.example.cpaas_telemo_flashcall_sdk

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class ApiResponse(val status: Int, val verification_status: String)
data class VerifyRequest(val to_call: String, val country: String)
data class VerifyRequestAfterCall(val call_id: String, val otp: String)

interface ApiService {

    @POST("flashCall/sent/{appId}")
    suspend fun startVerification(
        @Path("appId") appId: String,
        @Body request: VerifyRequest
    ): JsonObject

    @POST("flashCall/verify/{appId}")
    suspend fun verifyStatus(
        @Path("appId") appId: String,
        @Body request: VerifyRequestAfterCall
    ): ApiResponse
}