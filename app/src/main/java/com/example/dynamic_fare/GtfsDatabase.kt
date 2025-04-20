package com.example.dynamic_fare

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.dynamic_fare.auth.User
import com.example.dynamic_fare.auth.UserDao

@Database(
    entities = [Stop::class, Route::class, Matatu::class, MatatuFares::class, Fleet::class, QrCode::class, User::class],
    version = 8, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopDao(): StopsDao
    abstract fun routeDao(): RoutesDao
    abstract fun matatuDao(): MatatuDao
    abstract fun fareDao(): FareDao
    abstract fun fleetDao(): FleetDao
    abstract fun qrCodeDao(): QrCodeDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gtfs_database"
                ).addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Room migration for adding phone column to users table
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
            }
        }

        // Room migration for adding role column to users table
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
