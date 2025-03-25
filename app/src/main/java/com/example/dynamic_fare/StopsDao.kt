package com.example.dynamic_fare

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface StopsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStops(stops: List<Stop>)

    @Query("SELECT * FROM stops")
    suspend fun getAllStops(): List<Stop>
}