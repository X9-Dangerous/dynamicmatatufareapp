package com.example.dynamic_fare.data

import android.graphics.Bitmap
import com.example.dynamic_fare.utils.generateQRCodeBitmap
import com.google.firebase.database.*
import java.util.UUID

object MatatuRepository {

    fun fetchMatatuData(operatorId: String, onResult: (String, String, String) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("matatus").child(operatorId)

        dbRef.get().addOnSuccessListener { snapshot ->
            val regNumber = snapshot.child("regNumber").getValue(String::class.java) ?: ""
            val route = snapshot.child("route").getValue(String::class.java) ?: ""
            val uniqueCode = snapshot.child("uniqueCode").getValue(String::class.java) ?: ""

            onResult(regNumber, route, uniqueCode)
        }.addOnFailureListener {
            onResult("", "", "")
        }
    }

    fun fetchMatatusForOperator(operatorId: String, onResult: (List<String>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("matatus")

        database.orderByChild("operatorId").equalTo(operatorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matatus = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val regNumber = child.child("regNumber").getValue(String::class.java)
                        regNumber?.let { matatus.add(it) }
                    }
                    onResult(matatus)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList()) // Return empty list if query fails
                }
            })
    }

    fun saveMatatuDetails(
        operatorId: String,
        regNumber: String,
        route: String,
        onComplete: (String, Bitmap?) -> Unit
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("matatus").push()
        val uniqueCode = UUID.randomUUID().toString() // Generate a unique QR code identifier
        val qrBitmap = generateQRCodeBitmap(uniqueCode)

        val matatuData = mapOf(
            "operatorId" to operatorId,
            "regNumber" to regNumber,
            "route" to route,
            "uniqueCode" to uniqueCode
        )

        dbRef.setValue(matatuData).addOnSuccessListener {
            onComplete(uniqueCode, qrBitmap)
        }.addOnFailureListener {
            onComplete("", null) // Return null if saving fails
        }
    }
}
