package com.example.data.model

/**
 * Validation statuses strictly according to PRD section 7.1:
 * - APPROVED: The recipe passes all applicable configured checks.
 * - MODIFIED: The recipe was changed to address a restriction and has passed revalidation.
 * - WARNING: Relevant uncertainty or a concern needs attention. Explain the issue.
 * - REJECTED: The recipe violates a known critical restriction and must not be recommended.
 */
enum class ValidationStatus {
  APPROVED,
  MODIFIED,
  WARNING,
  REJECTED
}

/**
 * Metadata for a reviewed clinical dietary rule.
 */
data class SafetyRule(
  val ruleId: String,
  val ruleName: String,
  val version: String,
  val targetConditionOrAllergy: String,
  val reviewedBy: String = "Clinical Nutrition Review Panel",
  val reviewDate: String = "2026-08-20",
  val description: String
)

/**
 * Individual safety check result.
 */
data class SafetyCheckItem(
  val ruleId: String,
  val ruleName: String,
  val status: ValidationStatus,
  val message: String,
  val isCritical: Boolean = false
)

/**
 * Complete result of the deterministic meal safety validation engine.
 */
data class ValidationResult(
  val overallStatus: ValidationStatus,
  val primarySummary: String,
  val suitabilityExplanation: String,
  val checkItems: List<SafetyCheckItem>,
  val rulesApplied: List<String>,
  val appliedModifications: List<String> = emptyList(),
  val clinicalDisclaimer: String = "NutriPath validation evaluates deterministic rule-based dietary compliance and does not constitute medical diagnosis or individual physician prescription."
)
