package com.ities45.skycast.model.repository.map

import com.ities45.skycast.model.pojo.Place

interface IMapRepository {
    suspend fun searchPlace(query: String): Place?
}