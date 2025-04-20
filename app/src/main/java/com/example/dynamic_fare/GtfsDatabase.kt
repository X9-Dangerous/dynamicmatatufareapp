package com.example.dynamic_fare

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dynamic_fare.Route
import com.example.dynamic_fare.RoutesDao
import com.example.dynamic_fare.Stop
import com.example.dynamic_fare.StopsDao
import com.example.dynamic_fare.models.Matatu
import com.example.dynamic_fare.MatatuDao
import com.example.dynamic_fare.FareDao
import com.example.dynamic_fare.FleetDao
import com.example.dynamic_fare.QrCodeDao
import com.example.dynamic_fare.models.MatatuFares
import com.example.dynamic_fare.models.Fleet
import com.example.dynamic_fare.models.QrCode

@Database(entities = [Stop::class, Route::class, Matatu::class, MatatuFares::class, Fleet::class, QrCode::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopDao(): StopsDao
    abstract fun routeDao(): RoutesDao
    abstract fun matatuDao(): MatatuDao
    abstract fun fareDao(): FareDao
    abstract fun fleetDao(): FleetDao
    abstract fun qrCodeDao(): QrCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gtfs_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
