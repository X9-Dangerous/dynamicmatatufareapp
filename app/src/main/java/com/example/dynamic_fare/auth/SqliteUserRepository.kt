package com.example.dynamic_fare.auth

import android.content.Context
import com.example.dynamic_fare.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteUserRepository(private val context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun registerUser(name: String, email: String, password: String, phone: String = "", role: String): Boolean = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserByEmail(email)
        return@withContext if (existingUser == null) {
            userDao.insertUser(User(name = name, phone = phone, email = email, password = password, role = role))
            true
        } else {
            false // User already exists
        }
    }

    suspend fun authenticateUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
        userDao.authenticateUser(email, password)
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
    }

    suspend fun updatePasswordByEmail(email: String, newPassword: String) = withContext(Dispatchers.IO) {
        userDao.updatePasswordByEmail(email, newPassword)
    }
}
