package com.example.dynamicfare.utils

import com.google.firebase.database.*
import com.example.dynamic_fare.models.MatatuFares

fun loadFareData(matatuRegNo: String, callback: (MatatuFares?) -> Unit) {
    if (matatuRegNo.isEmpty()) {
        println("⚠️ Please enter Matatu registration number")
        return
    }

    FirebaseDatabase.getInstance().getReference("fares/$matatuRegNo")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.getValue(MatatuFares::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
}
