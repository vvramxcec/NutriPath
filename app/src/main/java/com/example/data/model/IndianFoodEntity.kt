package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1,000+ Indian Food Composition & Micronutrient Entity.
 * Sourced from Indian Food Nutrition Processed Dataset (Anuvaad & ICMR-NIN).
 * Stores comprehensive macro and micronutrients per 100g.
 */
@Entity(tableName = "indian_food_nutrition")
data class IndianFoodEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val dishName: String,
  val caloriesKcal: Float,
  val carbsGrams: Float,
  val proteinGrams: Float,
  val fatGrams: Float,
  val freeSugarGrams: Float,
  val fiberGrams: Float,
  val sodiumMg: Float,
  val calciumMg: Float,
  val ironMg: Float,
  val vitaminCMg: Float,
  val folateMcg: Float
)
