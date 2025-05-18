package com.example.frontend_happygreen.data

import com.google.gson.annotations.SerializedName

// Dettaglio del gruppo con membri
data class GroupDetail(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("owner") val ownerId: Int,
    @SerializedName("owner_details") val ownerDetails: UserData,
    val members: List<MembershipResponse>
)
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
 * Modello per i gruppi (classi)
 */
data class Group(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("owner") val ownerId: Int
)

data class GroupMembership(
    val id: Int? = null,
    @SerializedName("user") val userId: Int,
    @SerializedName("group") val groupId: Int
)

/**
 * Modelli per commenti
 */
data class Comment(
    val id: Int? = null,
    @SerializedName("post") val postId: Int,
    @SerializedName("user") val userId: Int,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null
)

/**
 * Modelli per oggetti rilevati
 */
data class DetectedObject(
    val id: Int? = null,
    @SerializedName("post") val postId: Int,
    val label: String,
    val description: String,
    @SerializedName("recycle_tips") val recycleTips: String
)

/**
 * Modelli per quiz
 */
data class Quiz(
    val id: Int? = null,
    val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    val options: List<String>
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

data class ClassRoom(
    val id: Int, // Rimosso il default di 0 che causava gli errori 404
    val name: String,
    val description: String = "",
    val backgroundImageId: Int,
    val teacherName: String = "Unknown Teacher",
    val memberCount: Int = 0,
    val ownerID: Int? = null,
    val userRole: String? = null
)

// Aggiungi a Models.kt
data class GroupDetailResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val owner: Int,
    @SerializedName("owner_details") val ownerDetails: UserData,
    val members: List<MemberData>
)

data class MemberData(
    val id: Int,
    val user: UserData,
    val role: String,
    @SerializedName("joined_at") val joinedAt: String
)

data class AddMemberRequest(
    @SerializedName("user_id") val userId: Int,
    val role: String = "student" // Valore predefinito: student
)

// Richiesta per rimuovere un membro da un gruppo
data class RemoveMemberRequest(
    @SerializedName("user_id") val userId: Int
)

// Richiesta per cambiare il ruolo di un membro
data class ChangeRoleRequest(
    @SerializedName("user_id") val userId: Int,
    val role: String
)

// Risposta alla richiesta di aggiungere un membro
data class MembershipResponse(
    val id: Int,
    @SerializedName("user") val userDetails: UserData,
    val role: String,
    @SerializedName("joined_at") val joinedAt: String
)

// Post corretto con imageUrl opzionale
data class Post(
    val id: Int? = null,
    @SerializedName("user") val userId: Int,
    @SerializedName("group") val groupId: Int,
    @SerializedName("image_url") val imageUrl: String = "https://example.com/placeholder.jpg", // Default URL
    val caption: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
)