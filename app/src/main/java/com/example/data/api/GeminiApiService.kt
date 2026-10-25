package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
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
data class GeminiGenerateRequest(
  val contents: List<GeminiContent>,
  val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  val temperature: Float? = 0.4f,
  val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
  val candidates: List<GeminiResponseCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponseCandidate(
  val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiIngredientItem(
  val displayName: String = "",
  val quantityGrams: Float = 50f,
  val displayUnit: String = "g",
  val preparationNote: String = ""
)

@JsonClass(generateAdapter = true)
data class GeminiMealRecipeItem(
  val title: String = "",
  val description: String = "",
  val mealType: String = "",
  val prepTimeMinutes: Int = 20,
  val servings: Int = 1,
  val calories: Float = 0f,
  val proteinGrams: Float = 0f,
  val carbsGrams: Float = 0f,
  val fatGrams: Float = 0f,
  val fiberGrams: Float = 0f,
  val sodiumMg: Float = 0f,
  val potassiumMg: Float = 0f,
  val ingredients: List<GeminiIngredientItem> = emptyList(),
  val preparationSteps: List<String> = emptyList(),
  val clinicalRationale: String = "",
  val adherenceTips: List<String> = emptyList(),
  val suitabilityNotes: String = ""
)

@JsonClass(generateAdapter = true)
data class GeminiGeneratedMealPlanResponse(
  val candidates: List<GeminiMealRecipeItem> = emptyList()
)

interface GeminiApiService {
  @POST("v1beta/models/gemini-3.5-flash:generateContent")
  suspend fun generateContent(
    @Query("key") apiKey: String,
    @Body request: GeminiGenerateRequest
  ): GeminiGenerateResponse

  companion object {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val moshi: Moshi by lazy {
      Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    }

    fun create(): GeminiApiService {
      val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
      }
      val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiService::class.java)
    }
  }
}
