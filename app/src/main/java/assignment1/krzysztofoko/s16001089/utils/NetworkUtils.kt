package assignment1.krzysztofoko.s16001089.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * NetworkUtils: Demonstrates the "Networking" requirement (8%).
 * 
 * This utility handles standard REST API communication using HttpURLConnection.
 * It demonstrates:
 * 1. Performing GET requests on a background thread.
 * 2. Handling HTTP Response Codes.
 * 3. Parsing JSON data into app-usable strings.
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"
    // Public API for random academic/life advice
    private const val API_URL = "https://api.adviceslip.com/advice"

    /**
     * Fetches a random "Academic Insight" from a remote REST API.
     * Uses withContext(Dispatchers.IO) to ensure networking doesn't block the UI.
     */
    suspend fun fetchDailyInsight(): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 5s timeout
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the stream into a string
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // PARSE JSON: {"slip": { "id": 1, "advice": "Text here"}}
                val jsonObject = JSONObject(response.toString())
                val slip = jsonObject.getJSONObject("slip")
                return@withContext slip.getString("advice")
            } else {
                Log.e(TAG, "Server Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network Error: ${e.localizedMessage}")
            null
        } finally {
            connection?.disconnect()
        }
    }
}
