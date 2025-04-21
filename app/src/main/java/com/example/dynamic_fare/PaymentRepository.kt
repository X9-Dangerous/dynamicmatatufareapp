package com.example.dynamic_fare

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dynamic_fare.models.Payment

class PaymentRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "payments_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PAYMENTS = "payments"
        private const val COL_ID = "id"
        private const val COL_USER_ID = "userId"
        private const val COL_AMOUNT = "amount"
        private const val COL_ROUTE = "route"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_STATUS = "status"
        private const val COL_START_LOCATION = "startLocation"
        private const val COL_END_LOCATION = "endLocation"
        private const val COL_MATATU_REG = "matatuRegistration"
        private const val COL_MPESA_RECEIPT = "mpesaReceiptNumber"
        private const val COL_PAYMENT_METHOD = "paymentMethod"
        private const val COL_PHONE_NUMBER = "phoneNumber"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PAYMENTS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_USER_ID TEXT,
                $COL_AMOUNT REAL,
                $COL_ROUTE TEXT,
                $COL_TIMESTAMP INTEGER,
                $COL_STATUS TEXT,
                $COL_START_LOCATION TEXT,
                $COL_END_LOCATION TEXT,
                $COL_MATATU_REG TEXT,
                $COL_MPESA_RECEIPT TEXT,
                $COL_PAYMENT_METHOD TEXT,
                $COL_PHONE_NUMBER TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        onCreate(db)
    }

    fun insertPayment(payment: Payment) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, payment.id)
            put(COL_USER_ID, payment.userId)
            put(COL_AMOUNT, payment.amount)
            put(COL_ROUTE, payment.route)
            put(COL_TIMESTAMP, payment.timestamp)
            put(COL_STATUS, payment.status)
            put(COL_START_LOCATION, payment.startLocation)
            put(COL_END_LOCATION, payment.endLocation)
            put(COL_MATATU_REG, payment.matatuRegistration)
            put(COL_MPESA_RECEIPT, payment.mpesaReceiptNumber)
            put(COL_PAYMENT_METHOD, payment.paymentMethod)
            put(COL_PHONE_NUMBER, payment.phoneNumber)
        }
        db.insert(TABLE_PAYMENTS, null, values)
        db.close()
    }

    fun getPaymentsForUser(userId: String): List<Payment> {
        val db = readableDatabase
        val payments = mutableListOf<Payment>()
        val cursor = db.query(
            TABLE_PAYMENTS,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId),
            null, null, "$COL_TIMESTAMP DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                payments.add(
                    Payment(
                        id = getString(getColumnIndexOrThrow(COL_ID)),
                        userId = getString(getColumnIndexOrThrow(COL_USER_ID)),
                        amount = getDouble(getColumnIndexOrThrow(COL_AMOUNT)),
                        route = getString(getColumnIndexOrThrow(COL_ROUTE)),
                        timestamp = getLong(getColumnIndexOrThrow(COL_TIMESTAMP)),
                        status = getString(getColumnIndexOrThrow(COL_STATUS)),
                        startLocation = getString(getColumnIndexOrThrow(COL_START_LOCATION)),
                        endLocation = getString(getColumnIndexOrThrow(COL_END_LOCATION)),
                        matatuRegistration = getString(getColumnIndexOrThrow(COL_MATATU_REG)),
                        mpesaReceiptNumber = getString(getColumnIndexOrThrow(COL_MPESA_RECEIPT)),
                        paymentMethod = getString(getColumnIndexOrThrow(COL_PAYMENT_METHOD)),
                        phoneNumber = getString(getColumnIndexOrThrow(COL_PHONE_NUMBER))
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return payments
    }

    fun deletePayment(paymentId: String) {
        val db = writableDatabase
        db.delete(TABLE_PAYMENTS, "$COL_ID = ?", arrayOf(paymentId))
        db.close()
    }
}
