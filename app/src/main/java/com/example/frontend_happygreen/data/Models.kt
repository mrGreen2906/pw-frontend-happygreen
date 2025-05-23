// Models.kt - Aggiornato con Like e Reactions completi

package com.example.frontend_happygreen.data

import com.google.gson.annotations.SerializedName

/**
 * Modelli per autenticazione
 */
data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserData
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = ""
)

data class RegisterResponse(
    val message: String,
    @SerializedName("user_id") val userId: Int
)

/**
 * Modello dati utente
 */
data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String? = "",
    @SerializedName("last_name") val lastName: String? = "",
    val avatar: String? = null,
    @SerializedName("eco_points") val ecoPoints: Int = 0,
    @SerializedName("date_joined") val dateJoined: String? = null,
    @SerializedName("email_verified") val emailVerified: Boolean = false
)

/**
 * Modello per i gruppi (classi) - AGGIORNATO con validazione migliorata
 */
data class Group(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val ownerName: String? = null, // NUOVO: Nome del proprietario
    val memberCount: Int? = null,  // NUOVO: Contatore dei membri
    val postCount: Int? = null,    // NUOVO: Contatore dei post
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("owner") val ownerId: Int
) {
    /**
     * Verifica se il gruppo ha dati validi
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && ownerId > 0 && (id == null || id > 0)
    }
}

data class GroupMembership(
    val id: Int? = null,
    @SerializedName("user") val userDetails: UserData,
    @SerializedName("group") val groupId: Int,
    val role: String = "student",
    @SerializedName("joined_at") val joinedAt: String? = null
)

/**
 * Modello ClassRoom con validazione robusta - AGGIORNATO
 */
data class ClassRoom(
    val id: Int,
    val name: String,
    val description: String = "",
    val backgroundImageId: Int,
    val teacherName: String = "",
    val memberCount: Int = 0,
    val postCount: Int = 0, // NUOVO: Contatore dei post reale
    val ownerID: Int? = null,
    val userRole: String = "member" // "admin", "teacher", "student", "member"
) {
    fun isValid(): Boolean {
        return id > 0 && name.isNotBlank()
    }
}

/**
 * Modelli per commenti - AGGIORNATO
 */
data class Comment(
    val id: Int? = null,
    @SerializedName("post") val postId: Int,
    @SerializedName("user") val userId: Int,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null
)

/**
 * Modelli per badge
 */
data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("icon_url") val iconUrl: String
)

data class UserBadge(
    val id: Int? = null,
    @SerializedName("user") val userId: Int,
    @SerializedName("badge") val badgeId: Int,
    @SerializedName("earned_at") val earnedAt: String? = null
)


data class GroupDetailResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val owner: Int,
    @SerializedName("owner_details") val ownerDetails: UserData,
    val members: List<MemberData>,
    val owner_name: String?,      // NUOVO: Nome del proprietario dal backend
    val member_count: Int?,       // NUOVO: Contatore membri dal backend
    val post_count: Int?          // NUOVO: Contatore post dal backend
)
fun GroupDetailResponse.toGroup(): Group {
    return Group(
        id = this.id,
        name = this.name,
        description = this.description,
        ownerId = this.owner,
        ownerName = this.owner_name,
        memberCount = this.member_count,
        postCount = this.post_count,
    )
}
data class MemberData(
    val id: Int,
    val user: UserData,
    val role: String,
    @SerializedName("joined_at") val joinedAt: String
)

// Richieste per gestione membri
data class RemoveMemberRequest(
    @SerializedName("user_id") val userId: Int
)

// Post con validazione migliorata e supporto completo
data class Post(
    val id: Int? = null,
    @SerializedName("user") val userId: Int,
    @SerializedName("group") val groupId: Int,
    @SerializedName("image_url") val imageUrl: String = "https://happygreen.example.com/default-placeholder.jpg",
    val caption: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null,

    // Campi per like, reactions e commenti (dal serializer)
    val comments: List<CommentResponse>? = null,
    val likes: List<LikeData>? = null,
    val reactions: List<ReactionData>? = null,
    @SerializedName("like_count") val likeCount: Int? = null,
    @SerializedName("comment_count") val commentCount: Int? = null,
    @SerializedName("user_liked") val userLiked: Boolean? = null,
    @SerializedName("user_reaction") val userReaction: String? = null
)

data class PostResponse(
    val id: Int,
    val user: UserData,
    @SerializedName("group") val groupId: Int,
    @SerializedName("image_url") val imageUrl: String?,
    val caption: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("created_at") val createdAt: String?,
    val comments: List<CommentResponse>? = null,
    val likes: List<LikeData>? = null,
    val reactions: List<ReactionData>? = null,
    @SerializedName("like_count") val likeCount: Int? = null,
    @SerializedName("comment_count") val commentCount: Int? = null,
    @SerializedName("user_liked") val userLiked: Boolean? = null,
    @SerializedName("user_reaction") val userReaction: String? = null
)

/**
 * NUOVO: Comment response con user details
 */
data class CommentResponse(
    val id: Int,
    val user: UserData,
    val content: String,
    @SerializedName("created_at") val createdAt: String
)
data class CreatePostRequest(
    @SerializedName("group") val groupId: Int,
    @SerializedName("image_url") val imageUrl: String = "https://happygreen.example.com/default-placeholder.jpg",
    val caption: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
/**
 * NUOVO: Like data dal server
 */
data class LikeData(
    val id: Int,
    val user: UserData,
    @SerializedName("created_at") val createdAt: String
)

/**
 * NUOVO: Reaction data dal server
 */
data class ReactionData(
    val id: Int,
    val user: UserData,
    val reaction: String,
    @SerializedName("created_at") val createdAt: String
)

/**
 * NUOVO: Responses per API calls
 */
data class LikeResponse(
    val liked: Boolean,
    @SerializedName("like_count") val likeCount: Int
)

data class ReactionResponse(
    val removed: Boolean,
    @SerializedName("user_reaction") val userReaction: String?,
    @SerializedName("reactions_count") val reactionsCount: Map<String, Int>
)

data class ReactionUser(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("created_at") val createdAt: String
)

/**
 * Utility per convertire Group in ClassRoom in modo sicuro
 */
fun Group.toClassRoom(currentUserId: Int? = null): ClassRoom? {
    val groupId = this.id
    if (groupId == null || groupId <= 0 || this.name.isBlank()) {
        return null
    }

    val isOwner = currentUserId == this.ownerId

    return ClassRoom(
        id = groupId,
        name = this.name,
        description = this.description ?: "",
        backgroundImageId = com.example.frontend_happygreen.R.drawable.happy_green_logo,
        teacherName = if (isOwner) "Tu (Proprietario)" else "Proprietario: ${this.ownerId}",
        memberCount = 0, // Sarà aggiornato quando disponibile
        ownerID = this.ownerId,
        userRole = if (isOwner) "admin" else "member"
    )
}

/**
 * Utility per validare una lista di ClassRoom
 */
fun List<ClassRoom>.filterValid(): List<ClassRoom> {
    return this.filter { it.isValid() }
}

/**
 * Extension per convertire PostResponse in Post locale
 */
fun PostResponse.toPost(): Post {
    return Post(
        id = this.id,
        userId = this.user.id,
        groupId = this.groupId,
        imageUrl = this.imageUrl ?: "https://happygreen.example.com/default-placeholder.jpg",
        caption = this.caption,
        latitude = this.latitude,
        longitude = this.longitude,
        createdAt = this.createdAt,
        comments = this.comments,
        likes = this.likes,
        reactions = this.reactions,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        userLiked = this.userLiked,
        userReaction = this.userReaction
    )
}