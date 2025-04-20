import android.content.Context
import com.example.dynamic_fare.auth.SqliteUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthManager {
    suspend fun signUpUser(
        context: Context,
        name: String,
        surname: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        selectedRole: String,
        termsAccepted: Boolean
    ): Boolean {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || selectedRole.isBlank()) {
            return false
        }

        if (password != confirmPassword) {
            return false
        }

        if (!termsAccepted) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val userRepository = SqliteUserRepository(context)
                val fullName = "$name $surname"
                android.util.Log.d("AuthManager", "Registering user: $fullName, Email: $email, Phone: $phone, Role: $selectedRole")
                val result = userRepository.registerUser(fullName, email, password, phone, selectedRole)
                android.util.Log.d("AuthManager", "Registration result: $result")
                true
            } catch (e: Exception) {
                android.util.Log.e("AuthManager", "Registration failed: ${e.message}")
                false
            }
        }
    }
}
