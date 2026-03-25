package com.example.carebridge.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Repository for user registration using Firebase Authentication and Realtime Database.
 */
class SignUpRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // If you get a "Default FirebaseApp is not initialized" or "Database URL not found" error,
    // you can pass your database URL directly to getInstance().
    // Example: FirebaseDatabase.getInstance("https://your-project-id.firebaseio.com/")
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    /**
     * Registers a new user in Firebase.
     */
    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        address: String
    ): Result<Unit> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password cannot be empty."))
            }

            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to get user ID.")

            // Store additional user data in Realtime Database
            val userMap = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phone" to phone,
                "address" to address,
                "email" to email
            )

            // Ensure the database reference is valid
            database.getReference("users").child(userId).setValue(userMap).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Log the exception to help debugging if needed
            Result.failure(e)
        }
    }
}
