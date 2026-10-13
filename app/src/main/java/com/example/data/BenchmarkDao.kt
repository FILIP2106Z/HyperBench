package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {

    @Query("SELECT * FROM benchmark_runs ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<BenchmarkEntity>>

    @Query("SELECT * FROM benchmark_runs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getRunsByProfile(profileId: Long): Flow<List<BenchmarkEntity>>

    @Query("SELECT * FROM benchmark_runs WHERE id = :id LIMIT 1")
    suspend fun getRunById(id: Long): BenchmarkEntity?

    @Query("SELECT * FROM benchmark_runs WHERE typeName = :typeName ORDER BY overallScore DESC LIMIT 1")
    suspend fun getBestRunForType(typeName: String): BenchmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: BenchmarkEntity): Long

    @Delete
    suspend fun deleteRun(run: BenchmarkEntity)

    @Query("DELETE FROM benchmark_runs")
    suspend fun clearAll()

    // User Profile Queries
    @Query("SELECT * FROM user_profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)
}
