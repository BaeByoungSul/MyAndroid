package com.myapp.myframedagger.common.retrofit_simple

import com.myapp.myframedagger.common.model.Album
import retrofit2.Response
import retrofit2.http.GET

interface AlbumService {

    @GET("/albums")
    suspend fun getAlbums(): Response<Album>

}