package com.example.nutriscanai.data.model

/**
 * Data class representing a User in the system.
 */
data class UserModel(
    val uid: String,
    val email: String,
    val displayName: String? = null
)
