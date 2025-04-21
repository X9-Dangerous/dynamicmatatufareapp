package com.example.dynamic_fare

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dynamic_fare.ui.screens.UserAccessibilitySettings

class UserSettingsRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "user_settings_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_SETTINGS = "user_settings"
        private const val COL_USER_ID = "userId"
        private const val COL_IS_DISABLED = "isDisabled"
        private const val COL_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        private const val COL_LAST_UPDATED = "lastUpdated"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COL_USER_ID TEXT PRIMARY KEY,
                $COL_IS_DISABLED INTEGER,
                $COL_NOTIFICATIONS_ENABLED INTEGER,
                $COL_LAST_UPDATED INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    fun getUserSettings(userId: String): UserAccessibilitySettings? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SETTINGS,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId),
            null, null, null
        )
        var settings: UserAccessibilitySettings? = null
        if (cursor.moveToFirst()) {
            settings = UserAccessibilitySettings(
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                isDisabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_DISABLED)) == 1,
                notificationsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIFICATIONS_ENABLED)) == 1,
                lastUpdated = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_UPDATED))
            )
        }
        cursor.close()
        db.close()
        return settings
    }

    fun saveUserSettings(settings: UserAccessibilitySettings) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, settings.userId)
            put(COL_IS_DISABLED, if (settings.isDisabled) 1 else 0)
            put(COL_NOTIFICATIONS_ENABLED, if (settings.notificationsEnabled) 1 else 0)
            put(COL_LAST_UPDATED, settings.lastUpdated)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }
}
