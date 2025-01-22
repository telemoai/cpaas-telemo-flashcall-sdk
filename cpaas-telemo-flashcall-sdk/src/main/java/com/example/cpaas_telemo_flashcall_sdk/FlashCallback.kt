package com.example.cpaas_telemo_flashcall_sdk

import javax.inject.Singleton

@Singleton
interface FlashCallback {
    fun onSuccess(s: String)
    fun onFailure(s: String)
}


