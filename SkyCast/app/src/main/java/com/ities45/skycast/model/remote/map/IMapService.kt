package com.ities45.skycast.model.remote.map

import com.ities45.skycast.model.pojo.Place
import retrofit2.http.GET
import retrofit2.http.Query

interface IMapService {
    @GET("search")
    suspend fun searchPlace(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<Place>
}