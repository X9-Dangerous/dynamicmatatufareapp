package com.example.dynamic_fare

import androidx.room.*
import com.example.dynamic_fare.models.Matatu

@Dao
interface MatatuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatatu(matatu: Matatu)

    @Query("SELECT * FROM matatu WHERE registrationNumber = :regNumber LIMIT 1")
    suspend fun getMatatuByRegistration(regNumber: String): Matatu?

    @Query("SELECT * FROM matatu")
    suspend fun getAllMatatus(): List<Matatu>

    @Query("SELECT * FROM matatu WHERE operatorId = :operatorId")
    suspend fun getMatatusForOperator(operatorId: String): List<Matatu>

    @Query("SELECT * FROM matatu WHERE matatuId = :matatuId LIMIT 1")
    suspend fun getMatatuById(matatuId: String): Matatu?

    @Delete
    suspend fun deleteMatatu(matatu: Matatu)
}
