package com.example.data

import android.content.Context
import com.example.R
import com.example.data.PresetHardwareDatabase
import com.example.model.BenchmarkRun
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class RankingEntry(
    @DocumentId val id: String = "",
    val rankingId: String = "",
    val userId: String = "",
    val userName: String = "Anonymous Tester",
    val userEmail: String = "",
    val deviceModel: String = "",
    val deviceManufacturer: String = "",
    val socChipset: String = "",
    val benchmarkType: String = "",
    val loadPreset: String = "",
    val overallScore: Long = 0L,
    val cpuScore: Long = 0L,
    val gpuScore: Long = 0L,
    val ramScore: Long = 0L,
    val diskScore: Long = 0L,
    val tierBadge: String = "S",
    @ServerTimestamp val createdAt: Timestamp? = null
)

class RankingRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
    }

    private val rankingsCollection by lazy {
        db.collection("rankings")
    }

    fun observeGlobalLeaderboard(limitCount: Long = 50): Flow<List<RankingEntry>> {
        return rankingsCollection
            .orderBy("overallScore", Query.Direction.DESCENDING)
            .limit(limitCount)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RankingEntry::class.java)
            }
    }

    fun observeUserRankings(userId: String): Flow<List<RankingEntry>> {
        return rankingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("overallScore", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RankingEntry::class.java)
            }
    }

    suspend fun submitRunToLeaderboard(run: BenchmarkRun, user: FirebaseUser): Result<String> {
        return try {
            val (badge, _) = PresetHardwareDatabase.getTierForScore(run.overallScore)
            val docId = "${user.uid}_${run.id}"

            val entry = hashMapOf<String, Any>(
                "rankingId" to docId,
                "userId" to user.uid,
                "userName" to (user.displayName ?: "Hardware Tester"),
                "userEmail" to (user.email ?: ""),
                "deviceModel" to run.deviceModel,
                "deviceManufacturer" to run.deviceManufacturer,
                "socChipset" to "${run.deviceManufacturer} ${run.deviceModel}",
                "benchmarkType" to run.type.name,
                "loadPreset" to run.loadPreset.label,
                "overallScore" to run.overallScore.toLong(),
                "cpuScore" to (run.cpuResult?.totalCpuScore?.toLong() ?: 0L),
                "gpuScore" to (run.gpuResult?.gpuScore?.toLong() ?: 0L),
                "ramScore" to (run.ramResult?.ramScore?.toLong() ?: 0L),
                "diskScore" to (run.diskResult?.diskScore?.toLong() ?: 0L),
                "tierBadge" to badge,
                "createdAt" to FieldValue.serverTimestamp()
            )

            rankingsCollection.document(docId).set(entry).await()
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
