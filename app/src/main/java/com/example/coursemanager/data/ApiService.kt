package com.example.coursemanager.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object ApiConfig {

    const val BASE_URL = "https://6ad63bf0f71f154ac486add2.mockapi.io/"
}

interface ApiService {
    @GET("categories") suspend fun getCategories(): List<Category>
    @POST("categories") suspend fun createCategory(@Body body: Category): Category
    @PUT("categories/{id}") suspend fun updateCategory(@Path("id") id: String, @Body body: Category): Category
    @DELETE("categories/{id}") suspend fun deleteCategory(@Path("id") id: String)

    @GET("courses") suspend fun getCourses(): List<Course>
    @GET("courses/{id}") suspend fun getCourse(@Path("id") id: String): Course
    @POST("courses") suspend fun createCourse(@Body body: Course): Course
    @PUT("courses/{id}") suspend fun updateCourse(@Path("id") id: String, @Body body: Course): Course
    @DELETE("courses/{id}") suspend fun deleteCourse(@Path("id") id: String)
}

object ApiClient {
    val service: ApiService by lazy {
        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
