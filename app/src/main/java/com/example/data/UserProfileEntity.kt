package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val username: String,
    val displayName: String,
    val deviceAlias: String,
    val avatarColorHex: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            username = username,
            displayName = displayName,
            deviceAlias = deviceAlias,
            avatarColorHex = avatarColorHex,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile, isCurrent: Boolean = false): UserProfileEntity {
            return UserProfileEntity(
                id = profile.id,
                username = profile.username,
                displayName = profile.displayName,
                deviceAlias = profile.deviceAlias,
                avatarColorHex = profile.avatarColorHex,
                createdAt = profile.createdAt,
                isCurrent = isCurrent
            )
        }
    }
}
