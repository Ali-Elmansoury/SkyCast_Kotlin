package com.ities45.skycast.model.remote.map

import com.ities45.skycast.model.pojo.Place

class MapRemoteDataSourceImpl(private val mapService: IMapService): IMapRemoteDataSource {
    override suspend fun searchPlace(query: String): Place? {
        return try {
            val places = mapService.searchPlace(query)
            places.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}