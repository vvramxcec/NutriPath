package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.HealthProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  suspend fun syncProfileToCloud(userId: String, profile: HealthProfile): Result<Unit> {
    return try {
      val data = mapOf(
        "name" to profile.name,
        "age" to profile.age,
        "gender" to profile.gender,
        "weightKg" to profile.weightKg,
        "heightCm" to profile.heightCm,
        "activityLevel" to profile.activityLevel,
        "medicalConditions" to profile.medicalConditions,
        "medications" to profile.medications,
        "allergies" to profile.allergies,
        "dietaryPreference" to profile.dietaryPreference,
        "nutritionGoal" to profile.nutritionGoal,
        "dailyCalorieTarget" to profile.dailyCalorieTarget,
        "proteinTargetG" to profile.proteinTargetG,
        "carbsTargetG" to profile.carbsTargetG,
        "fatTargetG" to profile.fatTargetG,
        "maxSodiumMg" to profile.maxSodiumMg,
        "maxPotassiumMg" to profile.maxPotassiumMg,
        "excludedIngredients" to profile.excludedIngredients,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      db.collection("users").document(userId)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteUserDataFromCloud(userId: String): Result<Unit> {
    return try {
      db.collection("users").document(userId).delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
