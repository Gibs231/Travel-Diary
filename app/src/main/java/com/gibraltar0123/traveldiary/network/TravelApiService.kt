package com.gibraltar0123.traveldiary.network

import com.gibraltar0123.traveldiary.model.ApiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "http://103.175.219.150:3010/"

// Create Moshi instance
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Configure Retrofit with converter
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface TravelApiService {
    @GET("travel")
    suspend fun getTravel(
        @Query("userId") userId:String
    ): ApiResponse

    @Multipart
    @POST ("travel")
    suspend fun  uploadTravel(

        @Query("userId") userId:String,
        @Part("title") title: RequestBody,
        @Part("description") RequestBody: RequestBody,
        @Part image: MultipartBody.Part
    ): ApiResponse

    @Multipart
    @PUT ("travel")
    suspend fun updateTravel(
        @Query("userId") userId:String,
        @Part("id") id: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part
    ): ApiResponse

    @DELETE("travel")
    suspend fun deleteTravel(
        @Query("userId") userId:String,
        @Query("id") id: Int
    ): ApiResponse


}


object TravelApi {
    val service: TravelApiService by lazy {
        retrofit.create(TravelApiService::class.java)
    }

    fun getTravelUrl(imageId: String): String {
        return "${BASE_URL}$imageId.jpg"
    }
    enum class ApiStatus { LOADING, SUCCESS, FAILED }
}