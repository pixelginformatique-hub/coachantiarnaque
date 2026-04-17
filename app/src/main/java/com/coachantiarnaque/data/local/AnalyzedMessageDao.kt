package com.coachantiarnaque.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour accéder aux messages analysés en base locale.
 */
@Dao
interface AnalyzedMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AnalyzedMessageEntity): Long

    @Query("SELECT * FROM analyzed_messages ORDER BY timestamp DESC LIMIT 10")
    fun getRecentMessages(): Flow<List<AnalyzedMessageEntity>>

    @Query("SELECT * FROM analyzed_messages ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessage(): Flow<AnalyzedMessageEntity?>

    @Query("SELECT * FROM analyzed_messages WHERE id = :id")
    suspend fun getById(id: Long): AnalyzedMessageEntity?

    @Query("DELETE FROM analyzed_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM analyzed_messages")
    suspend fun deleteAll()
}
