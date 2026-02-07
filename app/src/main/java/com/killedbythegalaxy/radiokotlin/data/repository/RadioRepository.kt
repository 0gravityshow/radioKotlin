package com.killedbythegalaxy.radiokotlin.data.repository

import com.killedbythegalaxy.radiokotlin.data.model.NowPlayingResponse
import com.killedbythegalaxy.radiokotlin.data.remote.AzuraCastApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepository @Inject constructor(
    private val azuraCastApi: AzuraCastApi
) {
    
    /**
     * Fetches now playing info once
     */
    suspend fun getNowPlaying(): Result<NowPlayingResponse> {
        return try {
            val response = azuraCastApi.getNowPlaying()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Provides a flow that polls now playing info every N seconds
     */
    fun observeNowPlaying(intervalMs: Long = 10_000): Flow<Result<NowPlayingResponse>> = flow {
        while (true) {
            emit(getNowPlaying())
            delay(intervalMs)
        }
    }
    
    companion object {
        const val STREAM_URL = AzuraCastApi.STREAM_URL
    }
}
