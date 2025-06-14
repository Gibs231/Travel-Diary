package com.gibraltar0123.traveldiary.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibraltar0123.traveldiary.model.Travel
import com.gibraltar0123.traveldiary.network.TravelApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Travel>())
        private set

    var status = MutableStateFlow(TravelApi.ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = TravelApi.ApiStatus.LOADING
            try {
                val response = TravelApi.service.getTravel(userId)
                if (response.status == 1) {
                    data.value = response.travels
                    status.value = TravelApi.ApiStatus.SUCCESS
                    errorMessage.value = null
                } else {
                    throw Exception(response.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = TravelApi.ApiStatus.FAILED
                errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun saveData(userId: String, title: String, description: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TravelApi.service.uploadTravel(
                    userId,
                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
                if (result.status == 1) {
                    retrieveData(userId)
                } else {
                    throw Exception(result.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(userId: String, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TravelApi.service.deleteTravel(userId, id)
                if (result.status == 1) {
                    retrieveData(userId)
                } else {
                    throw Exception(result.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
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
