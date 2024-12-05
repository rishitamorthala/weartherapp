package com.cs407.weartherapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

data class UserData(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val gender: String,
    val birthday: String
)

object AuthManager {
    private const val PREF_NAME = "user_prefs"
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun signUp(userData: UserData): Boolean {
        //username already exists?
        if (getUserByUsername(userData.username) != null) {
            return false
        }

        //save data
        val userKey = "user_${userData.username}"
        prefs.edit()
            .putString(userKey, gson.toJson(userData))
            .putString("current_user", userData.username)
            .apply()
        return true
    }

    fun login(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        if (user?.password == password) {
            prefs.edit().putString("current_user", username).apply()
            return true
        }
        return false
    }

    private fun getUserByUsername(username: String): UserData? {
        val userKey = "user_${username}"
        val userJson = prefs.getString(userKey, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserData::class.java)
        } else null
    }

    fun getCurrentUser(): UserData? {
        val currentUsername = prefs.getString("current_user", null)
        return if (currentUsername != null) {
            getUserByUsername(currentUsername)
        } else null
    }

    fun logout() {
        prefs.edit().remove("current_user").apply()
    }
}