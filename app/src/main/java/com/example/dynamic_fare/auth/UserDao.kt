package com.example.dynamic_fare.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun authenticateUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    suspend fun updatePasswordByEmail(email: String, newPassword: String)
}
