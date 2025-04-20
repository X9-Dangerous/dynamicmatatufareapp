package com.example.dynamic_fare.data

import android.content.Context
import com.example.dynamic_fare.models.Matatu
import com.example.dynamic_fare.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatatuRepository(context: Context) {
    private val matatuDao = AppDatabase.getDatabase(context).matatuDao()

    suspend fun registerMatatu(
        matatuId: String,
        operatorId: String,
        regNumber: String,
        routeStart: String,
        routeEnd: String,
        stops: List<String>,
        mpesaType: String,
        pochiNumber: String,
        paybillNumber: String,
        accountNumber: String,
        tillNumber: String,
        sendMoneyPhone: String,
        fleetname: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = matatuDao.getMatatuByRegistration(regNumber)
            if (existing != null) return@withContext false
            val matatu = Matatu(
                matatuId = matatuId,
                operatorId = operatorId,
                registrationNumber = regNumber,
                routeStart = routeStart,
                routeEnd = routeEnd,
                stops = stops,
                mpesaOption = mpesaType,
                pochiNumber = pochiNumber,
                paybillNumber = paybillNumber,
                accountNumber = accountNumber,
                tillNumber = tillNumber,
                sendMoneyPhone = sendMoneyPhone,
                fleetname = fleetname
            )
            matatuDao.insertMatatu(matatu)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMatatuByRegistration(regNumber: String): Matatu? = withContext(Dispatchers.IO) {
        matatuDao.getMatatuByRegistration(regNumber)
    }

    suspend fun getAllMatatus(): List<Matatu> = withContext(Dispatchers.IO) {
        matatuDao.getAllMatatus()
    }

    suspend fun deleteMatatu(matatu: Matatu) = withContext(Dispatchers.IO) {
        matatuDao.deleteMatatu(matatu)
    }

    suspend fun isValidKenyanPlate(plate: String): Boolean = withContext(Dispatchers.IO) {
        val regex = "^[Kk][A-Z]{2} [0-9]{3}[A-Z]$".toRegex()
        return@withContext regex.matches(plate)
    }

    suspend fun fetchMatatusForOperator(operatorId: String): List<Matatu> = withContext(Dispatchers.IO) {
        matatuDao.getMatatusForOperator(operatorId)
    }

    suspend fun fetchMatatuDetails(matatuId: String): Matatu? = withContext(Dispatchers.IO) {
        matatuDao.getMatatuById(matatuId)
    }

    suspend fun getMatatuIdByRegistration(registrationNumber: String): String? = withContext(Dispatchers.IO) {
        val matatu = matatuDao.getMatatuByRegistration(registrationNumber)
        matatu?.matatuId
    }
}
