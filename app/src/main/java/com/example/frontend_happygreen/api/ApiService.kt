// ApiService.kt - Aggiornato con Like e Reactions

package com.example.frontend_happygreen.api

import com.example.frontend_happygreen.data.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("groups/my_groups/")
    suspend fun getMyGroups(@Header("Authorization") token: String): Response<List<GroupWithCounters>>

    // AGGIORNATO: Endpoint per cercare tutti i gruppi con contatori
    @GET("groups/")
    suspend fun getGroups(@Header("Authorization") token: String): Response<List<GroupWithCounters>>

    // Altre chiamate API esistenti...
    @POST("groups/")
    suspend fun createGroup(
        @Body group: Group,
        @Header("Authorization") token: String
    ): Response<Group>

    @POST("groups/{groupId}/join/")
    suspend fun joinGroup(
        @Path("groupId") groupId: Int,
        @Header("Authorization") token: String
    ): Response<Any>

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
    suspend fun getUserById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<UserData>

    @PUT("users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userData: UserData,
        @Header("Authorization") token: String
    ): Response<UserData>

    // Groups

    @GET("group-memberships/")
    suspend fun getGroupMemberships(@Header("Authorization") token: String): Response<List<GroupMembership>>

    @GET("posts/")
    suspend fun getPosts(@Header("Authorization") token: String): Response<List<PostResponse>>

    @GET("posts/")
    suspend fun getGroupPosts(
        @Query("group") groupId: Int,
        @Header("Authorization") token: String
    ): Response<List<PostResponse>>

    // CORRETTO: Usa Post per POST requests (invio dati)
    @POST("posts/")
    suspend fun createPost(
        @Body post: CreatePostRequest,
        @Header("Authorization") token: String
    ): Response<PostResponse>

    @POST("posts/")
    suspend fun createPost(@Body post: Post, @Header("Authorization") token: String): Response<Post>

    // NUOVO: Like endpoint
    @POST("posts/{id}/toggle_like/")
    suspend fun togglePostLike(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<LikeResponse>

    // NUOVO: Reactions endpoint
    @POST("posts/{id}/add_reaction/")
    suspend fun addPostReaction(
        @Path("id") postId: Int,
        @Body reaction: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ReactionResponse>

    @GET("posts/{id}/reactions/")
    suspend fun getPostReactions(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, List<ReactionUser>>>

    // Comments
    @POST("comments/")
    suspend fun createComment(
        @Body comment: Comment,
        @Header("Authorization") token: String
    ): Response<Comment>

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

    // Groups management
    @GET("groups/{id}/")
    suspend fun getGroupById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<GroupDetailResponse>


    @DELETE("groups/{id}/remove_member/")
    suspend fun removeGroupMember(
        @Path("id") groupId: Int,
        @Body request: RemoveMemberRequest,
        @Header("Authorization") token: String
    ): Response<Unit>

}
data class GroupWithCounters(
    val id: Int?,
    val name: String,
    val description: String?,
    val created_at: String?,
    val owner: Int,
    val owner_name: String?,      // Nome del proprietario
    val member_count: Int?,       // Contatore membri reale
    val post_count: Int?          // Contatore post reale
) {
    // Converte in Group per compatibilità con il codice esistente
    fun toGroup(): Group {
        return Group(
            id = this.id,
            name = this.name,
            description = this.description,
            ownerId = this.owner,
            ownerName = this.owner_name,
            memberCount = this.member_count,
            postCount = this.post_count,
            createdAt = this.created_at
        )
    }
}
// Classi per le richieste e risposte
data class UpdatePointsRequest(
    val points: Int,
    val game_id: String
)

data class UpdatePointsResponse(
    val success: Boolean,
    val message: String,
    val total_points: Int
)
data class CreatePostRequest(
    @SerializedName("group") val groupId: Int,
    @SerializedName("image_url") val imageUrl: String = "https://happygreen.example.com/default-placeholder.jpg",
    val caption: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
data class LeaderboardResponse(
    val userId: Int,
    val username: String,
    val score: Int? = null,
    val ecoPoints: Int? = null,
    val avatar: String? = null
)

// NUOVO: Response per like
data class LikeResponse(
    val liked: Boolean,
    val likeCount: Int
)

// NUOVO: Response per reactions
data class ReactionResponse(
    val removed: Boolean,
    val userReaction: String?,
    val reactionsCount: Map<String, Int>
)

// NUOVO: User che ha messo reaction
data class ReactionUser(
    val userId: Int,
    val username: String,
    val createdAt: String
)

// AGGIORNATO: Post response completo con like, reactions e commenti
data class PostResponse(
    val id: Int,
    val user: UserData,
    val group: Int,
    val imageUrl: String?,
    val caption: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String?,
    val comments: List<CommentResponse>? = null,
    val likes: List<LikeData>? = null,
    val reactions: List<ReactionData>? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val userLiked: Boolean? = null,
    val userReaction: String? = null
)

// NUOVO: Comment response con user details
data class CommentResponse(
    val id: Int,
    val user: UserData,
    val content: String,
    val createdAt: String
)

// NUOVO: Like data
data class LikeData(
    val id: Int,
    val user: UserData,
    val createdAt: String
)

// NUOVO: Reaction data
data class ReactionData(
    val id: Int,
    val user: UserData,
    val reaction: String,
    val createdAt: String
)