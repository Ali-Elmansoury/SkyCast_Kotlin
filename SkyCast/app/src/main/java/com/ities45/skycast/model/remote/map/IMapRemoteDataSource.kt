package com.ities45.skycast.model.remote.map

import com.ities45.skycast.model.pojo.Place

interface IMapRemoteDataSource {
    suspend fun searchPlace(query: String): Place?
}