package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HealthProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthProfileDao {
  @Query("SELECT * FROM health_profile WHERE id = 1 LIMIT 1")
  fun getHealthProfile(): Flow<HealthProfile?>

  @Query("SELECT * FROM health_profile WHERE id = 1 LIMIT 1")
  suspend fun getHealthProfileOnce(): HealthProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateProfile(profile: HealthProfile)

  @Query("DELETE FROM health_profile")
  suspend fun deleteAll()
}
