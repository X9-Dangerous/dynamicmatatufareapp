package com.example.dynamic_fare.data

import android.graphics.Bitmap
import com.example.dynamic_fare.utils.QRCodeGenerator
import com.google.firebase.database.*
import java.util.UUID

object MatatuRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("matatus")

    fun fetchMatatuData(operatorId: String, onResult: (String, String, String) -> Unit) {
        database.child(operatorId).get().addOnSuccessListener { snapshot ->
            val regNumber = snapshot.child("regNumber").getValue(String::class.java) ?: ""
            val route = snapshot.child("route").getValue(String::class.java) ?: ""
            val uniqueCode = snapshot.child("uniqueCode").getValue(String::class.java) ?: ""

            onResult(regNumber, route, uniqueCode)
        }.addOnFailureListener {
            onResult("", "", "")
        }
    }

    fun fetchMatatusForOperator(operatorId: String, onResult: (List<String>) -> Unit) {
        database.orderByChild("operatorId").equalTo(operatorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matatus = snapshot.children.mapNotNull { it.child("regNumber").getValue(String::class.java) }
                    onResult(matatus)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    fun saveMatatuDetails(
        operatorId: String,
        regNumber: String,
        route: String,
        onComplete: (String, Bitmap?) -> Unit
    ) {
        val uniqueCode = UUID.randomUUID().toString()
        val qrBitmap = QRCodeGenerator.generateQRCode(uniqueCode)
        val matatuData = mapOf(
            "operatorId" to operatorId,
            "regNumber" to regNumber,
            "route" to route,
            "uniqueCode" to uniqueCode
        )

        database.push().setValue(matatuData).addOnSuccessListener {
            onComplete(uniqueCode, qrBitmap)
        }.addOnFailureListener {
            onComplete("", null)
        }
    }
}
