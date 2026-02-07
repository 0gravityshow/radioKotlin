package com.killedbythegalaxy.radiokotlin.data.remote

import com.killedbythegalaxy.radiokotlin.data.model.NowPlayingResponse
import retrofit2.http.GET

interface AzuraCastApi {
    
    @GET("/api/nowplaying/killed_by_the_galaxy_radio")
    suspend fun getNowPlaying(): NowPlayingResponse
    
    companion object {
        const val BASE_URL = "https://azuracast.killedbythegalaxy.com"
        const val STREAM_URL = "https://azuracast.killedbythegalaxy.com/listen/killed_by_the_galaxy_radio/stream"
    }
}
