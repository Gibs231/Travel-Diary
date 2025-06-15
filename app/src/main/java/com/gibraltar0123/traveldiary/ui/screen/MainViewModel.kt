package com.gibraltar0123.traveldiary.ui.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibraltar0123.traveldiary.model.Travel
import com.gibraltar0123.traveldiary.network.ApiStatus
import com.gibraltar0123.traveldiary.network.TravelApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.math.log

class MainViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Travel>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val response = TravelApi.service.getTravel(userId)
                println(response.message)
                if (response.status == 200) {
                    data.value = response.travels
                    status.value = ApiStatus.SUCCESS
                    errorMessage.value = null
                    Log.d("MainViewModel", "Data retrieved successfully: ${data.value.size} items")
                } else {
                    throw Exception(response.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
                errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }



    @SuppressLint("SuspiciousIndentation")
    fun saveData(userId: String, title: String, description: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TravelApi.service.postTravel(
                    userId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
//                if (result.status == 200) {

                    retrieveData(userId)
//                } else {
//                    throw Exception(result.message)
//                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(userId: String, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
               TravelApi.service.deleteTravel(id)
                    retrieveData(userId)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Delete failure: ${e.message}")
                errorMessage.value = "Error deleting: ${e.message}"
            }
        }
    }

    fun updateData(userId: String, travelId: Int, title: String, description: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TravelApi.service.updateTravel(
                    userId = userId,
                    id = travelId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = bitmap.toMultipartBody()
                )

                if (result.status == 200) {
                    retrieveData(userId)
                } else {
                    throw Exception(result.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Update failure: ${e.message}")
                errorMessage.value = "Error updating: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 70, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size
        )
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody
        )
    }

    fun clearMessage() {
        errorMessage.value = null
    }
}