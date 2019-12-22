package br.com.weatherapp.api

import br.com.weatherapp.entity.FindResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("find")
    fun find(
        @Query("units")
        unit: String,
        @Query("lang")
        lang: String,
        @Query("q")
        cityName: String,
        @Query("appid")
        appId: String
    ) : Call<FindResult>

    @GET("group")
    fun findFavorites(
        @Query("units")
        unit: String,
        @Query("lang")
        lang: String,
        @Query("id")
        cityName: String,
        @Query("appid")
        appId: String
    ) : Call<FindResult>

}