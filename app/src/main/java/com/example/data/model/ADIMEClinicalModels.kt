package com.example.data.model

/**
 * NutriOrion Clinical Architecture (arXiv:2602.18650).
 * Implements the Nutrition Care Process (NCP) ADIME standard:
 * Assessment, Diagnosis, Intervention, Monitoring & Evaluation.
 */
enum class ADIMEActionType {
  CONTINUE, // Positive reinforcement for safe, health-protective foods
  REPLACE,  // Corrective substitution (e.g. swap high-GI white rice with Kerala Matta rice)
  ADD       // Protective addition (e.g. add soluble fiber, flaxseeds, moringa)
}

data class ADIMEAssessment(
  val patientSummary: String,
  val biomarkersSummary: String, // BP, HbA1c, Serum Potassium, BMI
  val activeConditions: List<String>,
  val activeMedications: List<String>,
  val dailyCalorieTarget: Int,
  val maxSodiumMg: Int,
  val maxPotassiumMg: Int
)

data class ADIMEDiagnosis(
  val problemCode: String,
  val description: String,
  val etiology: String, // Underlying cause (e.g., "Related to impaired insulin sensitivity & polypharmacy")
  val signsSymptoms: String, // Objective evidence
  val sumPriorityScore: Float, // Severity-Urgency-Modifiability composite score
  val severityLevel: String // "Critical", "High", "Moderate", "Routine"
)

data class ADIMEInterventionDirective(
  val action: ADIMEActionType,
  val foodItem: String,
  val suggestedSubstitute: String? = null,
  val clinicalReason: String
)

data class ADIMEMonitoring(
  val keyParametersToTrack: List<String>,
  val evaluationSchedule: String,
  val warningThresholds: List<String>
)

data class ADIMEClinicalRecord(
  val recipeTitle: String,
  val assessment: ADIMEAssessment,
  val diagnoses: List<ADIMEDiagnosis>,
  val interventions: List<ADIMEInterventionDirective>,
  val monitoring: ADIMEMonitoring,
  val isFHIRReady: Boolean = true,
  val timestampMillis: Long = System.currentTimeMillis()
)
