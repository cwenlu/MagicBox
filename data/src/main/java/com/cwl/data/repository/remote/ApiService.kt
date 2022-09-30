package com.cwl.data.repository.remote

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @Author cwl
 * @Date 2022/3/11 8:22 下午
 * @Description
 */
interface ApiService {
    companion object {
        const val url = "https://www.wanandroid.com"
    }

    @GET("/article/list/{page}/json")
    suspend fun fetchArticleList(@Path("page") page: Int): String
}