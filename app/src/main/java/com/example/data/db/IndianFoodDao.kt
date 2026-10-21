package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.IndianFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndianFoodDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(foods: List<IndianFoodEntity>)

  @Query("SELECT COUNT(*) FROM indian_food_nutrition")
  suspend fun getCount(): Int

  @Query("SELECT * FROM indian_food_nutrition ORDER BY dishName ASC")
  fun getAllFoodsFlow(): Flow<List<IndianFoodEntity>>

  @Query("SELECT * FROM indian_food_nutrition ORDER BY dishName ASC")
  suspend fun getAllFoodsOnce(): List<IndianFoodEntity>

  @Query("SELECT * FROM indian_food_nutrition WHERE dishName LIKE '%' || :query || '%' LIMIT 100")
  suspend fun searchFoods(query: String): List<IndianFoodEntity>

  @Query("""
    SELECT * FROM indian_food_nutrition 
    WHERE caloriesKcal BETWEEN :minCal AND :maxCal 
      AND sodiumMg <= :maxSodium 
      AND freeSugarGrams <= :maxSugar
    ORDER BY fiberGrams DESC
    LIMIT :limit
  """)
  suspend fun getClinicalApprovedFoods(
    minCal: Float,
    maxCal: Float,
    maxSodium: Float,
    maxSugar: Float,
    limit: Int = 20
  ): List<IndianFoodEntity>

  @Query("SELECT * FROM indian_food_nutrition WHERE caloriesKcal BETWEEN :minCal AND :maxCal ORDER BY RANDOM() LIMIT :limit")
  suspend fun getRandomFoodsByCalorieRange(minCal: Float, maxCal: Float, limit: Int = 10): List<IndianFoodEntity>
}
