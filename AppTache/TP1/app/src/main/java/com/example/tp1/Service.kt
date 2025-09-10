package com.example.tp1

import org.kickmyb.transfer.AddTaskRequest
import org.kickmyb.transfer.HomeItemResponse
import org.kickmyb.transfer.SigninRequest
import org.kickmyb.transfer.SigninResponse
import org.kickmyb.transfer.SignupRequest
import org.kickmyb.transfer.TaskDetailResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Service {
    @POST("api/id/signup")
    fun signup(@Body signupRequest : SignupRequest): Call<SigninResponse>

    @POST("api/id/signin")
    fun signin(@Body signinRequest : SigninRequest): Call<SigninResponse>

    @POST("api/add")
    fun addTask(@Body addTaskRequest: AddTaskRequest): Call<Void>

    @GET("api/home")
    fun getTasks(): Call<List<HomeItemResponse>>

    @POST("api/id/signout")
    fun signout(): Call<Void>

    @GET("api/detail/{id}")
    fun getDetailTask(@Path("id") id: String): Call<TaskDetailResponse>

    @GET("api/progress/{id}/{valeur}")
    fun updateProgress(@Path("id") id: String, @Path("valeur") valeur: String): Call<Void>

    @POST("api/delete/{id}")
    fun deleteTask(@Path("id") id: String): Call<Void>
}