package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qrcodes")
data class QrCode(
    @PrimaryKey val registrationNumber: String,
    val qrImage: ByteArray
)
