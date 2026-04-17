package com.coachantiarnaque.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.coachantiarnaque.domain.model.ResultType

/**
 * Entité Room représentant un message analysé stocké en base locale.
 */
@Entity(tableName = "analyzed_messages")
@TypeConverters(Converters::class)
data class AnalyzedMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val senderNumber: String?,
    val score: Int,
    val resultType: ResultType,
    val reasons: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Convertisseurs Room pour les types complexes.
 */
class Converters {
    @TypeConverter
    fun fromResultType(value: ResultType): String = value.name

    @TypeConverter
    fun toResultType(value: String): ResultType = ResultType.valueOf(value)

    @TypeConverter
    fun fromReasons(value: List<String>): String = value.joinToString("|||")

    @TypeConverter
    fun toReasons(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }
}
