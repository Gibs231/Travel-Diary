package com.gibraltar0123.traveldiary.network

import com.gibraltar0123.traveldiary.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://103.175.219.150:3010/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface TravelApiService {
    @GET("travel")
    suspend fun getTravel(
        @Query("userId") userId: String
    ): OpStatus

    @Multipart
    @POST("travel")
    suspend fun postTravel(
        @Part("userId") userId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part
    )

    @Multipart
    @PUT("/travel/{id}")
    suspend fun updateTravel(
        @Part("userId") userId: String,
        @Path("id") id: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @DELETE("/travel/{id}")
    suspend fun deleteTravel(
        @Path("id") id: Int
    )
}

object TravelApi {
    val service: TravelApiService by lazy {
        retrofit.create(TravelApiService::class.java)
    }

    fun getTravelImageUrl(imageUrl: String): String {
        return if (imageUrl.startsWith("http://")) {
            imageUrl.replace("http://", "https://")
        } else if (!imageUrl.startsWith("https://") && imageUrl.isNotEmpty()) {
            "${BASE_URL}${imageUrl}"
        } else imageUrl
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }