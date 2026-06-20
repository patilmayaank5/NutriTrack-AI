package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// The target structure of the parsed foods
@JsonClass(generateAdapter = true)
data class DetectedFood(
    @Json(name = "foodName") val foodName: String,
    @Json(name = "quantityMultiplier") val quantityMultiplier: Float, // e.g. 2.0
    @Json(name = "quantityUnit") val quantityUnit: String, // "piece(s)", "g", "ml", "katori/cup"
    @Json(name = "calories") val calories: Float,
    @Json(name = "protein") val protein: Float,
    @Json(name = "carbs") val carbs: Float,
    @Json(name = "fats") val fats: Float,
    @Json(name = "fiber") val fiber: Float
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun cleanMarkdownJson(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json").substringBeforeLast("```")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").substringBeforeLast("```")
        }
        return clean.trim()
    }

    suspend fun analyzeMealText(mealText: String): List<DetectedFood> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API key is missing or not configured.")
            throw IllegalStateException("API key is missing or not configured.")
        }

        val promptText = """
            You are a nutrition expert specializing in Indian food.
            Analyze this description of a meal: "$mealText".
            Identify all distinct food items, estimate their quantity multipliers, quantity units, and standard realistic macro nutritional values (Calories, Protein in g, Carbs in g, Fats in g, and Fiber in g).
            Use standard Indian food items when possible.
            If user mentions "2 roti, dal and rice", you should split them into:
            1. "Roti (Plain)", quantity: 2.0 piece(s), calories ~170 kcal, protein ~6g, carbs ~36g, fats ~1g, fiber ~5g.
            2. "Yellow Dal (Tadka)", quantity: 1.0 katori/cup, calories ~150 kcal, protein ~7g, carbs ~22g, fats ~4g, fiber ~5g.
            3. "Basmati Rice (Cooked)", quantity: 100.0 g, calories ~130 kcal, protein ~2.7g, carbs ~28g, fats ~0.3g, fiber ~0.4g.

            You MUST strictly return a JSON array matching this format (no conversational text surrounding, just valid JSON array):
            [
              {
                "foodName": "Food Name string",
                "quantityMultiplier": 1.0,
                "quantityUnit": "piece(s) / g / ml / katori/cup / plate",
                "calories": 150.0,
                "protein": 7.0,
                "carbs": 22.0,
                "fats": 4.0,
                "fiber": 5.0
              }
            ]
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = promptText))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are an automated JSON-only API that parses natural text meal logs and outputs only valid JSON arrays according to the specification."))
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanedJson = cleanMarkdownJson(jsonText)
                val type = Types.newParameterizedType(List::class.java, DetectedFood::class.java)
                val listAdapter = moshi.adapter<List<DetectedFood>>(type)
                listAdapter.fromJson(cleanedJson) ?: emptyList()
            } else {
                throw Exception("Empty response from Gemini")
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error analyzing food with Gemini", e)
            throw e
        }
    }

    fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    suspend fun analyzeMealPhoto(bitmap: android.graphics.Bitmap, promptText: String): List<DetectedFood> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API key is missing or not configured for photo analysis.")
            throw IllegalStateException("API key is missing or not configured for photo analysis.")
        }

        val base64Image = bitmapToBase64(bitmap)
        val imagePart = GeminiPart(
            inlineData = InlineData(
                mimeType = "image/jpeg",
                data = base64Image
            )
        )
        val textPart = GeminiPart(text = promptText)

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(textPart, imagePart)
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are an automated JSON-only API that recognizes and analyzes food from pictures and outputs only valid JSON arrays matching the requested specification."))
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanedJson = cleanMarkdownJson(jsonText)
                val type = Types.newParameterizedType(List::class.java, DetectedFood::class.java)
                val listAdapter = moshi.adapter<List<DetectedFood>>(type)
                listAdapter.fromJson(cleanedJson) ?: emptyList()
            } else {
                throw Exception("Empty response from Gemini")
            }
        } catch (e: java.lang.Exception) {
            Log.e("GeminiClient", "Error analyzing food photo with Gemini", e)
            throw e
        }
    }
    
    suspend fun fetchDailyFitnessTip(): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API key is missing or not configured for fitness tip.")
            throw IllegalStateException("API key is missing or not configured for fitness tip.")
        }

        val promptText = """
            Provide a short, highly personalized fitness or diet tip tailored for an Indian palate.
            Fetch up-to-date, healthy alternatives for common Indian recipes (e.g., swapping refined flour for millet, baked alternatives to deep-fried snacks, updated research on traditional spices) using Google Search.
            Keep it strictly between 2 to 3 sentences in an engaging and accessible tone.
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = promptText)))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.5f // some creativity
            ),
            tools = listOf(GeminiTool(googleSearch = emptyMap()))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            jsonText?.trim() ?: throw Exception("Empty response from Gemini")
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error fetching fitness tip with Gemini", e)
            throw e
        }
    }

    suspend fun fetchWeeklySummary(metricsData: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing or not configured.")
        }

        val promptText = """
            You are an expert AI Nutrition Coach. The user is requesting a weekly health report summary based on their actual last 7 days of tracked data.
            
            Here is their data for the last 7 days:
            $metricsData
            
            Generate a concise, encouraging 1-paragraph summary reviewing their week. Highlight their "best day" or most consistent habit, summarize their average calories/protein, their hydration consistency, any weight trend (if logged), and note a specific area for improvement based on the provided actual data.
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = promptText)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            jsonText?.trim() ?: throw Exception("Empty response from Gemini")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun sendCoachMessage(
        contextPrefix: String,
        history: List<com.example.data.database.ChatMessageEntity>,
        newMessage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing or not configured.")
        }

        // We construct a simple multi-turn string for simplicity, or just use the system instruction
        val msgs = history.takeLast(10).joinToString("\n") {
            val roleName = if (it.role == "user") "User" else "Coach"
            "$roleName: ${it.content}"
        }

        val promptText = """
            $contextPrefix

            Previous conversation:
            $msgs

            User: $newMessage
            Coach:
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = promptText)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            jsonText?.trim() ?: throw Exception("Empty JSON response from AI Coach")
        } catch (e: Exception) {
            throw e
        }
    }
}
