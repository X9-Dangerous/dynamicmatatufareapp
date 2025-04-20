package com.example.dynamic_fare

import androidx.room.*
import com.example.dynamic_fare.models.QrCode

@Dao
interface QrCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCode)

    @Query("SELECT * FROM qrcodes WHERE registrationNumber = :registrationNumber LIMIT 1")
    suspend fun getQrCode(registrationNumber: String): QrCode?

    @Delete
    suspend fun deleteQrCode(qrCode: QrCode)
}
