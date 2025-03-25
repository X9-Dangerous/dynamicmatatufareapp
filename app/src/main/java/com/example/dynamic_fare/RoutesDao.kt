package com.example.dynamic_fare


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoutesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoutes(routes: List<Route>)

    @Query("SELECT * FROM routes")
    suspend fun getAllRoutes(): List<Route>
}