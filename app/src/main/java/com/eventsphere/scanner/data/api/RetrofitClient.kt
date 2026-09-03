package com.eventsphere.scanner.data.api

import com.eventsphere.scanner.data.local.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var api: EventSphereApi? = null
    private var currentBaseUrl: String? = null

    val apiService: EventSphereApi
        get() = api ?: throw IllegalStateException("RetrofitClient not initialized. Call getApi(preferencesManager) first.")

    fun getApi(preferencesManager: PreferencesManager): EventSphereApi {
        val baseUrl = preferencesManager.baseUrl.let { if (it.endsWith("/")) it else "$it/" }
        
        if (retrofit == null || currentBaseUrl != baseUrl) {
            synchronized(this) {
                if (retrofit == null || currentBaseUrl != baseUrl) {
                    currentBaseUrl = baseUrl
                    retrofit = buildRetrofit(baseUrl, preferencesManager)
                    api = retrofit?.create(EventSphereApi::class.java)
                }
            }
        }
        return api!!
    }

    private fun buildRetrofit(baseUrl: String, preferencesManager: PreferencesManager): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(preferencesManager))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(preferencesManager) { getApi(preferencesManager) })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Call this when baseUrl changes to force re-initialization on next getApi call.
     */
    fun reset() {
        synchronized(this) {
            retrofit = null
            api = null
            currentBaseUrl = null
        }
    }
}
