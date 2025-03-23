import com.google.firebase.database.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

data class MatatuFares(
    val offPeakFare: Double = 0.0,
    val peakFare: Double = 0.0,
    val rainyOffPeakFare: Double = 0.0,
    val rainyPeakFare: Double = 0.0
)

class FareManager(private val database: FirebaseDatabase) {

    fun fetchFares(matatuRegNo: String, callback: (MatatuFares?) -> Unit) {
        val ref = database.getReference("fares/$matatuRegNo")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fares = snapshot.getValue(MatatuFares::class.java)
                callback(fares)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun getFare(fares: MatatuFares?, isPeakHours: Boolean, isRaining: Boolean): Double {
        return when {
            isPeakHours && isRaining -> fares?.rainyPeakFare ?: 0.0
            isPeakHours -> fares?.peakFare ?: 0.0
            isRaining -> fares?.rainyOffPeakFare ?: 0.0
            else -> fares?.offPeakFare ?: 0.0
        }
    }
}

class WeatherManager(private val apiKey: String) {

    private val client = OkHttpClient()

    fun fetchWeather(city: String, callback: (Boolean) -> Unit) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false) // Assume no rain if API call fails
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val weather = json.getJSONArray("weather").getJSONObject(0).getString("main")
                    callback(weather == "Rain")
                } ?: callback(false)
            }
        })
    }
}

// 🚀 Example: Fetch Fare Based on Dynamic Weather and QR Scan
fun processQrScan(qrCodeData: String, city: String, isPeakHours: Boolean) {
    val database = FirebaseDatabase.getInstance()
    val fareManager = FareManager(database)
    val weatherManager = WeatherManager("d77ed3bf47a3594d4053bb96e601958f")

    val matatuRegNo = qrCodeData.trim()

    weatherManager.fetchWeather(city) { isRaining ->
        fareManager.fetchFares(matatuRegNo) { fares ->
            if (fares != null) {
                val finalFare = fareManager.getFare(fares, isPeakHours, isRaining)
                println("Final Fare for $matatuRegNo in $city (Rain: $isRaining): Ksh $finalFare")
            } else {
                println("⚠️ No fare data found for Matatu: $matatuRegNo")
            }
        }
    }
}
