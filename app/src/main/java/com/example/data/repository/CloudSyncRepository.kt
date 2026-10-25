package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.HealthProfile
import com.example.data.model.MealPlanEntity
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
        "weightKg" to profile.weightKg.toDouble(),
        "heightCm" to profile.heightCm.toDouble(),
        "activityLevel" to profile.activityLevel,
        "medicalConditions" to profile.medicalConditions,
        "medications" to profile.medications,
        "allergies" to profile.allergies,
        "dietaryPreference" to profile.dietaryPreference,
        "dietaryRestrictions" to profile.dietaryRestrictions,
        "nutritionGoal" to profile.nutritionGoal,
        "clinicianNotes" to profile.clinicianNotes,
        "dailyCalorieTarget" to profile.dailyCalorieTarget,
        "proteinTargetG" to profile.proteinTargetG,
        "carbsTargetG" to profile.carbsTargetG,
        "fatTargetG" to profile.fatTargetG,
        "maxSodiumMg" to profile.maxSodiumMg,
        "maxPotassiumMg" to profile.maxPotassiumMg,
        "systolicBp" to profile.systolicBp,
        "diastolicBp" to profile.diastolicBp,
        "hba1c" to profile.hba1c.toDouble(),
        "serumPotassium" to profile.serumPotassium.toDouble(),
        "excludedIngredients" to profile.excludedIngredients,
        "preferredIngredients" to profile.preferredIngredients,
        "consentGiven" to profile.consentGiven,
        "hasCompletedOnboarding" to profile.hasCompletedOnboarding,
        "lastUpdatedMillis" to profile.lastUpdatedMillis,
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

  suspend fun fetchProfileFromCloud(userId: String): Result<HealthProfile?> {
    return try {
      val doc = db.collection("users").document(userId).get().await()
      if (!doc.exists()) {
        return Result.success(null)
      }

      val data = doc.data ?: return Result.success(null)

      val profile = HealthProfile(
        id = 1,
        name = data["name"] as? String ?: "",
        age = (data["age"] as? Number)?.toInt() ?: 0,
        gender = data["gender"] as? String ?: "",
        weightKg = (data["weightKg"] as? Number)?.toFloat() ?: 0f,
        heightCm = (data["heightCm"] as? Number)?.toFloat() ?: 0f,
        activityLevel = data["activityLevel"] as? String ?: "Moderately Active",
        medicalConditions = (data["medicalConditions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        medications = (data["medications"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        allergies = (data["allergies"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        dietaryPreference = data["dietaryPreference"] as? String ?: "Balanced",
        dietaryRestrictions = (data["dietaryRestrictions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        nutritionGoal = data["nutritionGoal"] as? String ?: "Eat Healthy & Maintain",
        clinicianNotes = data["clinicianNotes"] as? String ?: "",
        dailyCalorieTarget = (data["dailyCalorieTarget"] as? Number)?.toInt() ?: 2000,
        proteinTargetG = (data["proteinTargetG"] as? Number)?.toInt() ?: 75,
        carbsTargetG = (data["carbsTargetG"] as? Number)?.toInt() ?: 250,
        fatTargetG = (data["fatTargetG"] as? Number)?.toInt() ?: 60,
        maxSodiumMg = (data["maxSodiumMg"] as? Number)?.toInt() ?: 2300,
        maxPotassiumMg = (data["maxPotassiumMg"] as? Number)?.toInt() ?: 3000,
        systolicBp = (data["systolicBp"] as? Number)?.toInt() ?: 120,
        diastolicBp = (data["diastolicBp"] as? Number)?.toInt() ?: 80,
        hba1c = (data["hba1c"] as? Number)?.toFloat() ?: 5.5f,
        serumPotassium = (data["serumPotassium"] as? Number)?.toFloat() ?: 4.0f,
        excludedIngredients = (data["excludedIngredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        preferredIngredients = (data["preferredIngredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        consentGiven = data["consentGiven"] as? Boolean ?: true,
        hasCompletedOnboarding = data["hasCompletedOnboarding"] as? Boolean ?: false,
        lastUpdatedMillis = (data["lastUpdatedMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
      )
      Result.success(profile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun syncMealPlanToCloud(userId: String, meal: MealPlanEntity): Result<Unit> {
    return try {
      val docId = "${meal.date}_${meal.mealType}"
      val data = mapOf(
        "date" to meal.date,
        "mealType" to meal.mealType,
        "recipeId" to meal.recipeId,
        "recipeTitle" to meal.recipeTitle,
        "servings" to meal.servings.toDouble(),
        "caloriesKcal" to meal.caloriesKcal.toDouble(),
        "proteinGrams" to meal.proteinGrams.toDouble(),
        "carbsGrams" to meal.carbsGrams.toDouble(),
        "fatGrams" to meal.fatGrams.toDouble(),
        "fiberGrams" to meal.fiberGrams.toDouble(),
        "sodiumMg" to meal.sodiumMg.toDouble(),
        "status" to meal.status,
        "validationStatus" to meal.validationStatus,
        "suitabilityExplanation" to meal.suitabilityExplanation,
        "ingredientsSummary" to meal.ingredientsSummary,
        "instructionsSummary" to meal.instructionsSummary,
        "timestamp" to meal.timestamp,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      db.collection("users").document(userId)
        .collection("meal_plans").document(docId)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun syncMealPlansToCloud(userId: String, meals: List<MealPlanEntity>): Result<Unit> {
    return try {
      val batch = db.batch()
      val mealPlansColl = db.collection("users").document(userId).collection("meal_plans")
      for (meal in meals) {
        val docId = "${meal.date}_${meal.mealType}"
        val docRef = mealPlansColl.document(docId)
        val data = mapOf(
          "date" to meal.date,
          "mealType" to meal.mealType,
          "recipeId" to meal.recipeId,
          "recipeTitle" to meal.recipeTitle,
          "servings" to meal.servings.toDouble(),
          "caloriesKcal" to meal.caloriesKcal.toDouble(),
          "proteinGrams" to meal.proteinGrams.toDouble(),
          "carbsGrams" to meal.carbsGrams.toDouble(),
          "fatGrams" to meal.fatGrams.toDouble(),
          "fiberGrams" to meal.fiberGrams.toDouble(),
          "sodiumMg" to meal.sodiumMg.toDouble(),
          "status" to meal.status,
          "validationStatus" to meal.validationStatus,
          "suitabilityExplanation" to meal.suitabilityExplanation,
          "ingredientsSummary" to meal.ingredientsSummary,
          "instructionsSummary" to meal.instructionsSummary,
          "timestamp" to meal.timestamp,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        batch.set(docRef, data, SetOptions.merge())
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchMealPlansFromCloud(userId: String): Result<List<MealPlanEntity>> {
    return try {
      val snapshot = db.collection("users").document(userId)
        .collection("meal_plans")
        .get()
        .await()

      val list = snapshot.documents.mapNotNull { doc ->
        val data = doc.data ?: return@mapNotNull null
        MealPlanEntity(
          id = 0, // Auto-generated locally on insertion
          date = data["date"] as? String ?: return@mapNotNull null,
          mealType = data["mealType"] as? String ?: return@mapNotNull null,
          recipeId = data["recipeId"] as? String ?: "",
          recipeTitle = data["recipeTitle"] as? String ?: "",
          servings = (data["servings"] as? Number)?.toFloat() ?: 1.0f,
          caloriesKcal = (data["caloriesKcal"] as? Number)?.toFloat() ?: 0f,
          proteinGrams = (data["proteinGrams"] as? Number)?.toFloat() ?: 0f,
          carbsGrams = (data["carbsGrams"] as? Number)?.toFloat() ?: 0f,
          fatGrams = (data["fatGrams"] as? Number)?.toFloat() ?: 0f,
          fiberGrams = (data["fiberGrams"] as? Number)?.toFloat() ?: 0f,
          sodiumMg = (data["sodiumMg"] as? Number)?.toFloat() ?: 0f,
          status = data["status"] as? String ?: MealPlanEntity.STATUS_PLANNED,
          validationStatus = data["validationStatus"] as? String ?: "APPROVED",
          suitabilityExplanation = data["suitabilityExplanation"] as? String ?: "",
          ingredientsSummary = data["ingredientsSummary"] as? String ?: "",
          instructionsSummary = data["instructionsSummary"] as? String ?: "",
          timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteMealFromCloud(userId: String, date: String, mealType: String): Result<Unit> {
    return try {
      val docId = "${date}_${mealType}"
      db.collection("users").document(userId)
        .collection("meal_plans").document(docId)
        .delete()
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteUserDataFromCloud(userId: String): Result<Unit> {
    return try {
      // 1. Delete all subcollection docs
      val mealsSnapshot = db.collection("users").document(userId)
        .collection("meal_plans")
        .get()
        .await()
      val batch = db.batch()
      for (doc in mealsSnapshot.documents) {
        batch.delete(doc.reference)
      }
      batch.delete(db.collection("users").document(userId))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
