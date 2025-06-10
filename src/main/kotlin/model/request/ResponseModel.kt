package com.github.SleekNekro.model.request

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.data.clases.UserData
import kotlinx.serialization.Serializable


@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class RegisterResponse(
    val user: UserData,
    val profilePicUrl: String,
    val message: String
)

@Serializable
data class ImageUploadResponse(
    val url: String
)

@Serializable
data class FollowResponse(
    val success: Boolean,
    val message: String
)
@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val profilePic: String?,
    val followers: Int
) {
    companion object {
        fun fromUser(user: UserData): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                profilePic = user.profilePic,
                followers = user.followers
            )
        }
    }
}
