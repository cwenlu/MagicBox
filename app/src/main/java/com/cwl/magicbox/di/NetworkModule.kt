package com.cwl.magicbox.di

import android.content.Context
import com.cwl.data.repository.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @Author cwl
 * @Date 2022/3/11 5:39 下午
 * @Description
 */
@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    /**
     * QaNetworkModule ｜ StandardNetworkModule 实际可以利用gradle进行源码区分，让debug时用QaNetworkModule达到日志输出，其他时候StandardNetworkModule直接屏蔽日志
     * @param context Context
     * @param interceptors Set<[@kotlin.jvm.JvmSuppressWildcards] Interceptor>
     * @return OkHttpClient
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        interceptors: Set<@JvmSuppressWildcards Interceptor>
    ): OkHttpClient = OkHttpClient.Builder()
        .apply { interceptors.forEach(::addInterceptor) }
        .cache(Cache(File(context.cacheDir, "api-cache"),/*10M*/10L * 1024 * 1024))
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ApiService.url)
            .build()

}