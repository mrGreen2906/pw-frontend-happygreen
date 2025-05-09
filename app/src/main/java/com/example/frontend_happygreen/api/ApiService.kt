package com.example.frontend_happygreen.api

import com.example.frontend_happygreen.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login/")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/resend-verification/")
    suspend fun resendVerification(@Body email: Map<String, String>): Response<Map<String, String>>

    @GET("auth/verify-email/{token}/")
    suspend fun verifyEmail(@Path("token") token: String): Response<Map<String, String>>

    @GET("users/me/")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserData>

    @GET("users/{id}/")
    suspend fun getUserById(@Path("id") id: Int, @Header("Authorization") token: String): Response<UserData>

    @PUT("users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userData: UserData,
        @Header("Authorization") token: String
    ): Response<UserData>

    // Groups
    @GET("groups/")
    suspend fun getGroups(@Header("Authorization") token: String): Response<List<Group>>

    @POST("groups/")
    suspend fun createGroup(@Body group: Group, @Header("Authorization") token: String): Response<Group>

    @GET("group-memberships/")
    suspend fun getGroupMemberships(@Header("Authorization") token: String): Response<List<GroupMembership>>

    // Posts
    @GET("posts/")
    suspend fun getPosts(@Header("Authorization") token: String): Response<List<Post>>

    @POST("posts/")
    suspend fun createPost(@Body post: Post, @Header("Authorization") token: String): Response<Post>

    // Badges
    @GET("badges/")
    suspend fun getBadges(@Header("Authorization") token: String): Response<List<Badge>>

    @GET("user-badges/")
    suspend fun getUserBadges(@Header("Authorization") token: String): Response<List<UserBadge>>

    @POST("auth/verify-otp/{userId}/")
    suspend fun verifyOTP(
        @Path("userId") userId: Int,
        @Body codeMap: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("api/leaderboard/")
    suspend fun getGlobalLeaderboard(
        @Header("Authorization") token: String
    ): Response<List<LeaderboardResponse>>

    @GET("api/leaderboard/")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String,
        @Query("game_id") gameId: String
    ): Response<List<LeaderboardResponse>>

    @POST("api/user/update-points/")
    suspend fun updateUserPoints(
        @Header("Authorization") token: String,
        @Body request: UpdatePointsRequest
    ): Response<UpdatePointsResponse>
}

// Classi per le richieste e risposte
data class UpdatePointsRequest(
    val points: Int,
    val game_id: String  // Modificato per match con il backend
)

data class UpdatePointsResponse(
    val success: Boolean,
    val message: String,
    val total_points: Int  // Modificato per match con il backend
)

data class LeaderboardResponse(
    val userId: Int,
    val username: String,
    val score: Int? = null,  // Per classifiche di gioco specifiche
    val ecoPoints: Int? = null,  // Per classifica globale
    val avatar: String? = null
)