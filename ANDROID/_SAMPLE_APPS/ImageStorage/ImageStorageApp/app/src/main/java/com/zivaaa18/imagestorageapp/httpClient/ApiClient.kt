package com.zivaaa18.imagestorageapp.httpClient

import com.zivaaa18.imagestorageapp.models.AccessToken
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Request


/**
 * Used to set AccessToken in header
 */
class ApiInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .also {
                // required for normal working
                ApiClient.interceptAccessTokenHeader(it)
            }
            .build()


        return chain.proceed(newRequest)
    }
}

/**
 * Api Factory.
 */
class ApiClient private constructor() {
    companion object {
        val BASE_URL = "http://192.168.100.2:3000/api/"

        val HEADER_ACCESS_TOKEN = "X-Access-Token"
        val ONE_IMAGE_FIELD = "userfile"

        /**
         * create api with included interceptor. Note that it has to handle access token!
         * @see ApiInterceptor
         */
        fun api(interceptor : Interceptor? = null, token: AccessToken? = null): Api {
            setAccessToken(token)

            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder().addInterceptor(interceptor ?: ApiInterceptor()).build()
                )
                .build()
                .create(Api::class.java)
        }

        private var token : AccessToken? = null

        fun setAccessToken(token : AccessToken?) {
            this.token = token
        }

        fun getAccessToken() : AccessToken? {
            return token
        }

        fun interceptAccessTokenHeader(builder : Request.Builder) {
            builder.header(ApiClient.HEADER_ACCESS_TOKEN, getAccessToken()?.id ?: "")
        }
    }
}