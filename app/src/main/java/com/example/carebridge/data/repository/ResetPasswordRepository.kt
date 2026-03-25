package com.example.carebridge.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository for password reset operations using Firebase.
 */
class ResetPasswordRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Sends a password reset email to the user.
     * Note: Firebase handle password reset via email for security.
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty."))
            }
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
