package com.example.carebridge.data.local

import android.content.Context
import com.example.carebridge.data.model.UserModel

/**
 * A persistent database using SharedPreferences to store registered users.
 * This ensures credentials survive app restarts.
 */
object UserDatabase {
    private const val PREFS_NAME = "CareBridgeUsers"

    /**
     * Registers a new user. Returns false if the username already exists.
     */
    fun register(context: Context, username: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(username)) return false
        
        prefs.edit().putString(username, password).apply()
        return true
    }

    /**
     * Validates credentials and returns the UserModel if successful.
     */
    fun login(context: Context, username: String, password: String): UserModel? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedPassword = prefs.getString(username, null)
        
        return if (storedPassword == password) {
            UserModel(
                uid = username,
                email = "$username@example.com",
                displayName = username
            )
        } else {
            null
        }
    }

    /**
     * Updates the password for a user.
     * Returns true if successful, false if username doesn't exist or old password matches.
     */
    fun updatePassword(context: Context, username: String, oldPassword: String, newPassword: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedPassword = prefs.getString(username, null)
        
        return if (storedPassword != null && storedPassword == oldPassword) {
            prefs.edit().putString(username, newPassword).apply()
            true
        } else {
            false
        }
    }
}
