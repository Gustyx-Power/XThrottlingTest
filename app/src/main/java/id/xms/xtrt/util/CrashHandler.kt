package id.xms.xtrt.util

import android.util.Log

object CrashHandler {
    private const val TAG = "XThrottlingTest"

    fun <T> safeExecute(operation: () -> T, fallback: T, description: String = "operation"): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to execute $description: ${e.message}")
            fallback
        }
    }

    suspend fun <T> safeExecuteSuspend(operation: suspend () -> T, fallback: T, description: String = "operation"): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to execute $description: ${e.message}")
            fallback
        }
    }

    fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    fun logWarning(message: String) {
        Log.w(TAG, message)
    }
}
