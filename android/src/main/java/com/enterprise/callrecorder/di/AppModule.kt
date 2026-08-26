package com.enterprise.callrecorder.di

import android.content.Context
import com.enterprise.callrecorder.data.CallRecordingDatabase
import com.enterprise.callrecorder.service.CallRecordingManager
import com.enterprise.callrecorder.service.RecordingUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.enterprise.callrecorder.api.CallRecordingApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dependency Injection Module
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCallRecordingDatabase(
        @ApplicationContext context: Context
    ): CallRecordingDatabase {
        return CallRecordingDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCallRecordingManager(
        @ApplicationContext context: Context
    ): CallRecordingManager {
        return CallRecordingManager(context)
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://your-backend-server.com/")  // À configurer
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCallRecordingApiService(retrofit: Retrofit): CallRecordingApiService {
        return retrofit.create(CallRecordingApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRecordingUploader(
        @ApplicationContext context: Context,
        apiService: CallRecordingApiService
    ): RecordingUploader {
        return RecordingUploader(context, apiService)
    }
}
