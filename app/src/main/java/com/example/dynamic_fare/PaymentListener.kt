import android.util.Log
import com.google.firebase.database.*

class PaymentListener {
    private val database = FirebaseDatabase.getInstance()
    private val paymentsRef = database.getReference("payments")

    fun startListening() {
        paymentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (paymentSnapshot in snapshot.children) {
                    val paymentId = paymentSnapshot.key
                    val amount = paymentSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                    val timestamp = paymentSnapshot.child("time").getValue(Long::class.java) ?: 0L
                    val carDetails = paymentSnapshot.child("carDetails").getValue(String::class.java) ?: ""
                    val matatuDetails = paymentSnapshot.child("matatuDetails").getValue(String::class.java) ?: ""

                    if (isPaymentValid(amount, timestamp)) {
                        sendPaymentNotification(paymentId ?: "", amount, carDetails, matatuDetails)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read payments", error.toException())
            }
        })
    }

    private fun isPaymentValid(amount: Double, timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        return amount > 0 && timestamp in (currentTime - 86400000)..currentTime // Payment must be recent (last 24 hours)
    }

    private fun sendPaymentNotification(paymentId: String, amount: Double, carDetails: String, matatuDetails: String) {
        val fcmRef = FirebaseDatabase.getInstance().getReference("notifications")
        val notificationData = mapOf(
            "title" to "Payment Received",
            "message" to "KES $amount received for $carDetails ($matatuDetails).",
            "paymentId" to paymentId
        )
        fcmRef.push().setValue(notificationData)
    }
}
