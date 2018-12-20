package com.zivaaa18.imagestorageapp.httpClient

import com.zivaaa18.imagestorageapp.httpClient.responses.SharedImageResponse
import com.zivaaa18.imagestorageapp.httpClient.responses.SimpleSuccessResponse
import com.zivaaa18.imagestorageapp.models.AccessToken
import com.zivaaa18.imagestorageapp.models.Credentials
import com.zivaaa18.imagestorageapp.models.SharedImage
import com.zivaaa18.imagestorageapp.models.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface Api {
    @POST("users/login")
    fun login(@Body credentials: Credentials): Call<AccessToken>

    @POST("users/logout")
    fun logout(): Call<ResponseBody>

    @GET("users/me")
    fun me(): Call<User>

    @GET("images/my")
    fun getImages(): Call<SharedImageResponse>

    @Multipart
    @POST("images/upload")
    fun uploadImage(@Part file: MultipartBody.Part): Call<SharedImage>

    @DELETE("images/{id}")
    fun deleteImage(@Path("id") id: String): Call<SimpleSuccessResponse>
}