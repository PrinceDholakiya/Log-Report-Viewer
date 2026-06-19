package com.interview.logviewer.data.remote

import com.interview.logviewer.data.remote.dto.LogResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApiService {

    @GET("v0/b/fieldinspectiondev.firebasestorage.app/o/data%2Flogs_5k.json")
    suspend fun getLogs(
        @Query("alt") alt: String = "media",
        @Query("token") token: String = "15c66bf6-9716-44da-b3d1-ba9bb241baf8"
    ): LogResponseDto

    companion object {
        const val BASE_URL = "https://firebasestorage.googleapis.com/"
    }
}
