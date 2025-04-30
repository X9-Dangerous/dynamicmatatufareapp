package com.example.dynamic_fare.auth

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteUserRepository(private val context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun registerUser(name: String, email: String, password: String, phone: String = "", role: String): Boolean = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserByEmail(email)
        return@withContext if (existingUser == null) {
            val user = User(name = name, phone = phone, email = email, password = password, role = role)
            val rowId = userDao.insertUser(user)
            // Fetch the user again to get the auto-generated id
            val savedUser = userDao.getUserByEmail(email)
            Log.d("SqliteUserRepository", "Signed up user: id=${savedUser?.id}, email=${savedUser?.email}")
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
