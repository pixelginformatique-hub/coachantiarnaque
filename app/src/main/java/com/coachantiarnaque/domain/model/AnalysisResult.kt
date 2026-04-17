package com.coachantiarnaque.domain.model

/**
 * Catégorie de résultat d'analyse d'un message.
 */
enum class ResultType {
    SAFE,       // 🟢 Sécuritaire
    SUSPICIOUS, // 🟠 Attention
    SCAM        // 🔴 Arnaque probable
}

/**
 * Résultat complet de l'analyse d'un message.
 */
data class AnalysisResult(
    val score: Int,
    val resultType: ResultType,
    val reasons: List<String>
) {
    companion object {
        fun fromScore(score: Int, reasons: List<String>): AnalysisResult {
            val type = when {
                score >= 4 -> ResultType.SCAM
                score >= 2 -> ResultType.SUSPICIOUS
                else -> ResultType.SAFE
            }
            return AnalysisResult(score = score, resultType = type, reasons = reasons)
        }
    }
}
