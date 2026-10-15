package com.example.data.repository

import com.example.data.db.HealthProfileDao
import com.example.data.model.HealthProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthProfileRepository(private val healthProfileDao: HealthProfileDao) {

  val profileFlow: Flow<HealthProfile> = healthProfileDao.getHealthProfile().map { dbProfile ->
    dbProfile ?: HealthProfile()
  }

  suspend fun getProfileOnce(): HealthProfile {
    return healthProfileDao.getHealthProfileOnce() ?: HealthProfile().also {
      healthProfileDao.insertOrUpdateProfile(it)
    }
  }

  suspend fun saveProfile(profile: HealthProfile) {
    healthProfileDao.insertOrUpdateProfile(profile.copy(lastUpdatedMillis = System.currentTimeMillis()))
  }

  suspend fun resetProfile() {
    healthProfileDao.deleteAll()
    healthProfileDao.insertOrUpdateProfile(HealthProfile())
  }
}
