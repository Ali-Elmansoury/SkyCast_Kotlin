package com.ities45.skycast.model.repository.map

import com.ities45.skycast.model.pojo.Place
import com.ities45.skycast.model.remote.map.IMapRemoteDataSource

class MapRepositoryImpl(private val mapRemoteDataSource: IMapRemoteDataSource) : IMapRepository{

    companion object{
        private var instance : MapRepositoryImpl? = null
        fun getInstance(mapRemoteDataSource: IMapRemoteDataSource): MapRepositoryImpl {
            if (instance == null){
                instance = MapRepositoryImpl(mapRemoteDataSource)
            }
            return instance!!
        }
    }

    override suspend fun searchPlace(query: String): Place? {
        return mapRemoteDataSource.searchPlace(query)
    }

}