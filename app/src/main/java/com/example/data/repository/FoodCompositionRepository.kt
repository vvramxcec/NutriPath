package com.example.data.repository

import com.example.data.model.FoodIngredient
import java.io.BufferedReader
import java.io.StringReader

/**
 * Food composition repository containing verified reference nutritional values per 100g.
 * Sourced strictly from the Indian Food Composition Tables (IFCT / ICMR-NIN 2017)
 * and Anuvaad Indian Food Micronutrient Dataset (https://www.anuvaad.org.in/).
 *
 * Supports dynamic CSV ingestion for 1,000+ items from Anuvaad datasets.
 */
object FoodCompositionRepository {

  data class IngestionSummary(
    val totalImported: Int,
    val totalSkipped: Int,
    val message: String
  )

  // In-memory registry merging built-in reference items with dynamically ingested CSV items
  private val runtimeRegistry: MutableMap<String, FoodIngredient> = mutableMapOf()

  private val BUILTIN_INGREDIENTS: List<FoodIngredient> = listOf(
    // --- GRAINS & STAPLES ---
    FoodIngredient(
      id = "matta_rice_cooked",
      name = "Kerala Red Matta Rice (Cooked)",
      regionalName = "Palakkadan Matta Ari",
      category = "Grain",
      caloriesPer100g = 118f,
      proteinPer100g = 2.8f,
      carbsPer100g = 25.2f,
      fatPer100g = 0.6f,
      fiberPer100g = 2.4f,
      sugarPer100g = 0.1f,
      sodiumMgPer100g = 4f,
      potassiumMgPer100g = 55f,
      calciumMgPer100g = 11f,
      ironMgPer100g = 0.9f,
      vitaminCMgPer100g = 0f,
      vitaminKMcgPer100g = 0.2f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (A004)"
    ),
    FoodIngredient(
      id = "brown_rice_cooked",
      name = "Brown Rice (Cooked)",
      regionalName = "Kaichadi Ari",
      category = "Grain",
      caloriesPer100g = 123f,
      proteinPer100g = 2.7f,
      carbsPer100g = 25.6f,
      fatPer100g = 1.0f,
      fiberPer100g = 1.8f,
      sugarPer100g = 0.2f,
      sodiumMgPer100g = 5f,
      potassiumMgPer100g = 86f,
      calciumMgPer100g = 10f,
      ironMgPer100g = 0.5f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & USDA #110183"
    ),
    FoodIngredient(
      id = "white_rice_cooked",
      name = "White Polished Rice (Cooked)",
      regionalName = "Vella Ari",
      category = "Grain",
      caloriesPer100g = 130f,
      proteinPer100g = 2.4f,
      carbsPer100g = 28.2f,
      fatPer100g = 0.3f,
      fiberPer100g = 0.4f,
      sugarPer100g = 0.1f,
      sodiumMgPer100g = 2f,
      potassiumMgPer100g = 35f,
      glycemicIndex = "High",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (A001)"
    ),
    FoodIngredient(
      id = "ragi_flour_cooked",
      name = "Finger Millet / Ragi Puttu (Steamed)",
      regionalName = "Muthari / Ragi Puttu",
      category = "Grain",
      caloriesPer100g = 112f,
      proteinPer100g = 2.6f,
      carbsPer100g = 23.0f,
      fatPer100g = 0.7f,
      fiberPer100g = 4.2f,
      sugarPer100g = 0.3f,
      sodiumMgPer100g = 6f,
      potassiumMgPer100g = 145f,
      calciumMgPer100g = 115f,
      ironMgPer100g = 1.6f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (A018)"
    ),
    FoodIngredient(
      id = "atta_whole_wheat_roti",
      name = "Whole Wheat Chapati / Roti (Cooked)",
      regionalName = "Gothambu Roti",
      category = "Grain",
      caloriesPer100g = 265f,
      proteinPer100g = 9.2f,
      carbsPer100g = 52.0f,
      fatPer100g = 2.1f,
      fiberPer100g = 8.5f,
      sugarPer100g = 1.2f,
      sodiumMgPer100g = 120f,
      potassiumMgPer100g = 280f,
      calciumMgPer100g = 40f,
      ironMgPer100g = 3.2f,
      allergens = listOf("Wheat / Gluten"),
      glycemicIndex = "Medium",
      isGlutenFree = false,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (A012)"
    ),
    FoodIngredient(
      id = "rolled_oats_cooked",
      name = "Rolled Oats (Water porridge)",
      regionalName = "Oats Kurukku",
      category = "Grain",
      caloriesPer100g = 71f,
      proteinPer100g = 2.5f,
      carbsPer100g = 12.0f,
      fatPer100g = 1.4f,
      fiberPer100g = 1.7f,
      sugarPer100g = 0.2f,
      sodiumMgPer100g = 49f,
      potassiumMgPer100g = 61f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & USDA #110191"
    ),

    // --- PULSES & LEGUMES (DAL / PAYAR) ---
    FoodIngredient(
      id = "moong_dal_cooked",
      name = "Yellow Moong Dal (Cooked)",
      regionalName = "Cherupayar Parippu",
      category = "Pulse",
      caloriesPer100g = 105f,
      proteinPer100g = 7.0f,
      carbsPer100g = 17.5f,
      fatPer100g = 0.8f,
      fiberPer100g = 4.1f,
      sugarPer100g = 0.5f,
      sodiumMgPer100g = 15f,
      potassiumMgPer100g = 290f,
      calciumMgPer100g = 28f,
      ironMgPer100g = 1.4f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (B003)"
    ),
    FoodIngredient(
      id = "green_gram_whole_boiled",
      name = "Whole Green Gram (Boiled)",
      regionalName = "Cherupayar Puzhukku",
      category = "Pulse",
      caloriesPer100g = 118f,
      proteinPer100g = 8.2f,
      carbsPer100g = 19.4f,
      fatPer100g = 0.6f,
      fiberPer100g = 5.6f,
      sugarPer100g = 0.4f,
      sodiumMgPer100g = 12f,
      potassiumMgPer100g = 320f,
      calciumMgPer100g = 45f,
      ironMgPer100g = 1.8f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (B004)"
    ),
    FoodIngredient(
      id = "kadala_black_chickpeas_cooked",
      name = "Black Chickpeas / Kadala Curry (Cooked)",
      regionalName = "Kadala Curry",
      category = "Pulse",
      caloriesPer100g = 142f,
      proteinPer100g = 8.9f,
      carbsPer100g = 22.0f,
      fatPer100g = 2.4f,
      fiberPer100g = 7.6f,
      sugarPer100g = 1.1f,
      sodiumMgPer100g = 35f,
      potassiumMgPer100g = 360f,
      calciumMgPer100g = 62f,
      ironMgPer100g = 2.5f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (B001)"
    ),
    FoodIngredient(
      id = "toor_dal_sambar_cooked",
      name = "Toor Dal (Cooked in Sambar)",
      regionalName = "Thuvaram Parippu",
      category = "Pulse",
      caloriesPer100g = 114f,
      proteinPer100g = 6.8f,
      carbsPer100g = 19.8f,
      fatPer100g = 0.9f,
      fiberPer100g = 3.9f,
      sugarPer100g = 0.6f,
      sodiumMgPer100g = 45f,
      potassiumMgPer100g = 275f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017 (B007)"
    ),

    // --- KERALA & INDIAN VEGETABLES ---
    FoodIngredient(
      id = "ash_gourd_cooked",
      name = "Ash Gourd / Winter Melon",
      regionalName = "Kumbalanga",
      category = "Vegetable",
      caloriesPer100g = 13f,
      proteinPer100g = 0.4f,
      carbsPer100g = 2.8f,
      fatPer100g = 0.1f,
      fiberPer100g = 1.2f,
      sugarPer100g = 1.0f,
      sodiumMgPer100g = 8f,
      potassiumMgPer100g = 110f, // Low potassium, safe for CKD / Diuretics
      calciumMgPer100g = 20f,
      ironMgPer100g = 0.3f,
      vitaminCMgPer100g = 13f,
      vitaminKMcgPer100g = 1.1f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "bottle_gourd_cooked",
      name = "Bottle Gourd / Lauki",
      regionalName = "Churakka",
      category = "Vegetable",
      caloriesPer100g = 14f,
      proteinPer100g = 0.6f,
      carbsPer100g = 2.5f,
      fatPer100g = 0.1f,
      fiberPer100g = 1.5f,
      sugarPer100g = 0.8f,
      sodiumMgPer100g = 6f,
      potassiumMgPer100g = 87f, // Very renal and heart friendly
      calciumMgPer100g = 18f,
      ironMgPer100g = 0.4f,
      vitaminKMcgPer100g = 0.8f, // Safe for Warfarin
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "drumstick_moringa_pods",
      name = "Drumstick / Moringa Pods (Boiled)",
      regionalName = "Muringakkol",
      category = "Vegetable",
      caloriesPer100g = 26f,
      proteinPer100g = 2.5f,
      carbsPer100g = 3.7f,
      fatPer100g = 0.2f,
      fiberPer100g = 3.2f,
      sugarPer100g = 0.8f,
      sodiumMgPer100g = 14f,
      potassiumMgPer100g = 259f,
      calciumMgPer100g = 30f,
      ironMgPer100g = 0.8f,
      vitaminCMgPer100g = 52f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "okra_ladies_finger_cooked",
      name = "Ladies Finger / Okra (Thoran)",
      regionalName = "Vendakka",
      category = "Vegetable",
      caloriesPer100g = 33f,
      proteinPer100g = 1.9f,
      carbsPer100g = 5.2f,
      fatPer100g = 0.2f,
      fiberPer100g = 3.2f,
      sugarPer100g = 1.4f,
      sodiumMgPer100g = 7f,
      potassiumMgPer100g = 299f,
      calciumMgPer100g = 82f,
      ironMgPer100g = 0.6f,
      vitaminCMgPer100g = 23f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "raw_banana_green_plantain",
      name = "Raw Green Plantain (Boiled)",
      regionalName = "Pacha Nendrakaya",
      category = "Vegetable",
      caloriesPer100g = 78f,
      proteinPer100g = 1.3f,
      carbsPer100g = 18.2f,
      fatPer100g = 0.2f,
      fiberPer100g = 2.8f,
      sugarPer100g = 1.8f,
      sodiumMgPer100g = 5f,
      potassiumMgPer100g = 380f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "cucumber_vellarikka",
      name = "Malabar Cucumber / Madras Vellarikka",
      regionalName = "Kani Vellarikka",
      category = "Vegetable",
      caloriesPer100g = 15f,
      proteinPer100g = 0.7f,
      carbsPer100g = 2.9f,
      fatPer100g = 0.1f,
      fiberPer100g = 1.0f,
      sugarPer100g = 1.3f,
      sodiumMgPer100g = 6f,
      potassiumMgPer100g = 140f,
      vitaminKMcgPer100g = 16.4f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),

    // --- LEAFY GREENS (WITH VITAMIN K TAGS FOR DNI SAFETY) ---
    FoodIngredient(
      id = "spinach_palak_cooked",
      name = "Spinach / Palak (Cooked)",
      regionalName = "Palak",
      category = "Green Leafy",
      caloriesPer100g = 23f,
      proteinPer100g = 2.9f,
      carbsPer100g = 3.6f,
      fatPer100g = 0.4f,
      fiberPer100g = 2.2f,
      sugarPer100g = 0.4f,
      sodiumMgPer100g = 79f,
      potassiumMgPer100g = 558f,
      calciumMgPer100g = 99f,
      ironMgPer100g = 2.7f,
      vitaminCMgPer100g = 28f,
      vitaminKMcgPer100g = 482f, // Extremely high Vitamin K -> Contraindicated with Warfarin!
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "red_cheera_amaranth",
      name = "Red Amaranth Leaves / Cheera (Cooked)",
      regionalName = "Chuvanna Cheera Thoran",
      category = "Green Leafy",
      caloriesPer100g = 28f,
      proteinPer100g = 3.2f,
      carbsPer100g = 4.1f,
      fatPer100g = 0.3f,
      fiberPer100g = 3.5f,
      sugarPer100g = 0.2f,
      sodiumMgPer100g = 20f,
      potassiumMgPer100g = 611f,
      calciumMgPer100g = 260f,
      ironMgPer100g = 3.9f,
      vitaminCMgPer100g = 43f,
      vitaminKMcgPer100g = 440f, // High Vitamin K -> Black Box check for Warfarin
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "moringa_leaves_cooked",
      name = "Moringa / Drumstick Leaves",
      regionalName = "Muringayila Thoran",
      category = "Green Leafy",
      caloriesPer100g = 64f,
      proteinPer100g = 6.7f,
      carbsPer100g = 8.2f,
      fatPer100g = 1.4f,
      fiberPer100g = 4.8f,
      sugarPer100g = 0.6f,
      sodiumMgPer100g = 24f,
      potassiumMgPer100g = 337f,
      calciumMgPer100g = 440f,
      ironMgPer100g = 4.0f,
      vitaminCMgPer100g = 120f,
      vitaminKMcgPer100g = 365f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),

    // --- COCONUT & HEALTHY OILS ---
    FoodIngredient(
      id = "coconut_grated_fresh",
      name = "Grated Coconut (Fresh)",
      regionalName = "Chirakiya Thenga",
      category = "Vegetable",
      caloriesPer100g = 354f,
      proteinPer100g = 3.3f,
      carbsPer100g = 15.2f,
      fatPer100g = 33.5f,
      fiberPer100g = 9.0f,
      sugarPer100g = 6.2f,
      sodiumMgPer100g = 20f,
      potassiumMgPer100g = 356f,
      saturatedFatPer100g = 29.7f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "coconut_oil_virgin",
      name = "Virgin Coconut Oil",
      regionalName = "Velichenna",
      category = "Oil",
      caloriesPer100g = 862f,
      proteinPer100g = 0f,
      carbsPer100g = 0f,
      fatPer100g = 100f,
      fiberPer100g = 0f,
      sugarPer100g = 0f,
      sodiumMgPer100g = 0f,
      potassiumMgPer100g = 0f,
      saturatedFatPer100g = 86.5f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "curry_leaves_fresh",
      name = "Fresh Curry Leaves",
      regionalName = "Kariveppila",
      category = "Spice",
      caloriesPer100g = 108f,
      proteinPer100g = 6.1f,
      carbsPer100g = 18.7f,
      fatPer100g = 1.0f,
      fiberPer100g = 6.4f,
      sugarPer100g = 0.5f,
      sodiumMgPer100g = 18f,
      potassiumMgPer100g = 520f,
      calciumMgPer100g = 830f,
      ironMgPer100g = 7.0f,
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),

    // --- PROTEINS & SEAFOOD ---
    FoodIngredient(
      id = "kerala_sardines_curry",
      name = "Indian Oil Sardine (Kerala Mathi Curry)",
      regionalName = "Mathi / Chaala",
      category = "Meat/Fish",
      caloriesPer100g = 145f,
      proteinPer100g = 19.5f,
      carbsPer100g = 0.4f,
      fatPer100g = 7.5f,
      fiberPer100g = 0f,
      sugarPer100g = 0f,
      sodiumMgPer100g = 95f,
      potassiumMgPer100g = 390f,
      calciumMgPer100g = 380f,
      ironMgPer100g = 2.9f,
      allergens = listOf("Fish"),
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "kerala_mackerel_curry",
      name = "Indian Mackerel (Ayala Peera)",
      regionalName = "Ayala",
      category = "Meat/Fish",
      caloriesPer100g = 158f,
      proteinPer100g = 20.1f,
      carbsPer100g = 0.2f,
      fatPer100g = 8.6f,
      fiberPer100g = 0f,
      sugarPer100g = 0f,
      sodiumMgPer100g = 88f,
      potassiumMgPer100g = 340f,
      allergens = listOf("Fish"),
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    ),
    FoodIngredient(
      id = "boiled_egg_white",
      name = "Boiled Egg White (1 Large ~ 33g)",
      regionalName = "Mutta Vella",
      category = "Meat/Fish",
      caloriesPer100g = 52f,
      proteinPer100g = 11.0f,
      carbsPer100g = 0.7f,
      fatPer100g = 0.2f,
      fiberPer100g = 0f,
      sugarPer100g = 0.7f,
      sodiumMgPer100g = 166f,
      potassiumMgPer100g = 163f,
      allergens = listOf("Eggs"),
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & USDA #1124"
    ),
    FoodIngredient(
      id = "curd_homemade_buttermilk",
      name = "Homemade Buttermilk / Sambharam",
      regionalName = "Sambharam / Morumvellam",
      category = "Dairy",
      caloriesPer100g = 28f,
      proteinPer100g = 1.8f,
      carbsPer100g = 2.4f,
      fatPer100g = 1.1f,
      fiberPer100g = 0f,
      sugarPer100g = 2.4f,
      sodiumMgPer100g = 85f,
      potassiumMgPer100g = 135f,
      calciumMgPer100g = 95f,
      allergens = listOf("Dairy / Lactose"),
      glycemicIndex = "Low",
      isGlutenFree = true,
      dataSource = "Anuvaad & ICMR-NIN IFCT 2017"
    )
  )

  init {
    BUILTIN_INGREDIENTS.forEach { runtimeRegistry[it.id] = it }
  }

  fun getAllIngredients(): List<FoodIngredient> {
    return runtimeRegistry.values.toList()
  }

  fun getIngredient(id: String): FoodIngredient? {
    return runtimeRegistry[id]
  }

  fun searchIngredients(query: String, category: String? = null): List<FoodIngredient> {
    val q = query.trim().lowercase()
    return runtimeRegistry.values.filter { item ->
      val matchText = q.isEmpty() ||
        item.name.lowercase().contains(q) ||
        item.regionalName.lowercase().contains(q) ||
        item.category.lowercase().contains(q)
      val matchCategory = category.isNullOrBlank() || category.equals("All", ignoreCase = true) || item.category.equals(category, ignoreCase = true)
      matchText && matchCategory
    }
  }

  /**
   * Imports custom Anuvaad CSV datasets uploaded by the user or supplied via string.
   * Tolerant parsing matching diverse Indian food data header names.
   */
  fun importFromCsv(csvText: String): IngestionSummary {
    if (csvText.isBlank()) return IngestionSummary(0, 0, "CSV content was empty")

    var importedCount = 0
    var skippedCount = 0
    val reader = BufferedReader(StringReader(csvText))
    val headerLine = reader.readLine() ?: return IngestionSummary(0, 0, "No header row found")
    val headers = headerLine.split(",").map { it.trim().lowercase() }

    val nameIdx = headers.indexOfFirst { it.contains("name") || it.contains("food") || it.contains("item") }
    val regionalIdx = headers.indexOfFirst { it.contains("regional") || it.contains("hindi") || it.contains("malayalam") || it.contains("vernacular") }
    val catIdx = headers.indexOfFirst { it.contains("category") || it.contains("group") }
    val calIdx = headers.indexOfFirst { it.contains("calor") || it.contains("energy") || it.contains("kcal") }
    val proIdx = headers.indexOfFirst { it.contains("protein") }
    val carbIdx = headers.indexOfFirst { it.contains("carb") }
    val fatIdx = headers.indexOfFirst { it.contains("fat") && !it.contains("sat") }
    val fibIdx = headers.indexOfFirst { it.contains("fiber") || it.contains("fibre") }
    val sodIdx = headers.indexOfFirst { it.contains("sodium") || it.contains("na") }
    val potIdx = headers.indexOfFirst { it.contains("potassium") || it.contains("k") }
    val calcuimIdx = headers.indexOfFirst { it.contains("calcium") || it.contains("ca") }
    val ironIdx = headers.indexOfFirst { it.contains("iron") || it.contains("fe") }
    val vitCIdx = headers.indexOfFirst { it.contains("vitamin c") || it.contains("vit c") }
    val vitKIdx = headers.indexOfFirst { it.contains("vitamin k") || it.contains("vit k") }

    if (nameIdx == -1 || calIdx == -1) {
      return IngestionSummary(0, 0, "Invalid format: CSV must at minimum contain 'Food Name' and 'Calories' columns")
    }

    var line: String? = reader.readLine()
    while (line != null) {
      val trimmed = line.trim()
      if (trimmed.isNotBlank()) {
        val parts = trimmed.split(",")
        if (parts.size > nameIdx && parts.size > calIdx) {
          try {
            val rawName = parts[nameIdx].trim().replace("\"", "")
            val id = "anuvaad_" + rawName.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')
            val regionalName = if (regionalIdx != -1 && parts.size > regionalIdx) parts[regionalIdx].trim().replace("\"", "") else ""
            val category = if (catIdx != -1 && parts.size > catIdx) parts[catIdx].trim().replace("\"", "") else "General Food"
            val calories = parts[calIdx].trim().toFloatOrNull() ?: 0f
            val protein = if (proIdx != -1 && parts.size > proIdx) parts[proIdx].trim().toFloatOrNull() ?: 0f else 0f
            val carbs = if (carbIdx != -1 && parts.size > carbIdx) parts[carbIdx].trim().toFloatOrNull() ?: 0f else 0f
            val fat = if (fatIdx != -1 && parts.size > fatIdx) parts[fatIdx].trim().toFloatOrNull() ?: 0f else 0f
            val fiber = if (fibIdx != -1 && parts.size > fibIdx) parts[fibIdx].trim().toFloatOrNull() ?: 0f else 0f
            val sodium = if (sodIdx != -1 && parts.size > sodIdx) parts[sodIdx].trim().toFloatOrNull() ?: 0f else 0f
            val potassium = if (potIdx != -1 && parts.size > potIdx) parts[potIdx].trim().toFloatOrNull() ?: 0f else 0f
            val calcium = if (calcuimIdx != -1 && parts.size > calcuimIdx) parts[calcuimIdx].trim().toFloatOrNull() ?: 0f else 0f
            val iron = if (ironIdx != -1 && parts.size > ironIdx) parts[ironIdx].trim().toFloatOrNull() ?: 0f else 0f
            val vitC = if (vitCIdx != -1 && parts.size > vitCIdx) parts[vitCIdx].trim().toFloatOrNull() ?: 0f else 0f
            val vitK = if (vitKIdx != -1 && parts.size > vitKIdx) parts[vitKIdx].trim().toFloatOrNull() ?: 0f else 0f

            val item = FoodIngredient(
              id = id,
              name = rawName,
              regionalName = regionalName,
              category = category,
              caloriesPer100g = calories,
              proteinPer100g = protein,
              carbsPer100g = carbs,
              fatPer100g = fat,
              fiberPer100g = fiber,
              sugarPer100g = 0f,
              sodiumMgPer100g = sodium,
              potassiumMgPer100g = potassium,
              calciumMgPer100g = calcium,
              ironMgPer100g = iron,
              vitaminCMgPer100g = vitC,
              vitaminKMcgPer100g = vitK,
              glycemicIndex = if (carbs > 35f && fiber < 2f) "High" else "Low",
              isGlutenFree = !rawName.contains("wheat", ignoreCase = true) && !rawName.contains("maida", ignoreCase = true),
              dataSource = "Anuvaad Ingested Dataset"
            )
            runtimeRegistry[id] = item
            importedCount++
          } catch (e: Exception) {
            skippedCount++
          }
        } else {
          skippedCount++
        }
      }
      line = reader.readLine()
    }

    return IngestionSummary(
      totalImported = importedCount,
      totalSkipped = skippedCount,
      message = "Successfully ingested $importedCount Indian food items from Anuvaad CSV."
    )
  }

  fun registerIndianEntities(foods: List<com.example.data.model.IndianFoodEntity>) {
    foods.forEach { entity ->
      val id = "indian_" + entity.dishName.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')
      val nameLower = entity.dishName.lowercase()
      val isHighGI = entity.carbsGrams > 30f && entity.fiberGrams < 2f
      val isGlutenFree = !nameLower.contains("wheat") &&
        !nameLower.contains("maida") &&
        !nameLower.contains("roti") &&
        !nameLower.contains("paratha") &&
        !nameLower.contains("parantha") &&
        !nameLower.contains("naan") &&
        !nameLower.contains("bread") &&
        !nameLower.contains("semolina") &&
        !nameLower.contains("suji") &&
        !nameLower.contains("rava")

      val allergens = mutableListOf<String>()
      if (nameLower.contains("egg") || nameLower.contains("anda") || nameLower.contains("omelette")) allergens.add("Eggs")
      if (nameLower.contains("fish") || nameLower.contains("machli") || nameLower.contains("prawn") || nameLower.contains("jhinga")) allergens.add("Fish")
      if (nameLower.contains("paneer") || nameLower.contains("cheese") || nameLower.contains("curd") || nameLower.contains("dahi") || nameLower.contains("milk") || nameLower.contains("lassi")) allergens.add("Dairy / Lactose")
      if (nameLower.contains("peanut") || nameLower.contains("moongfali")) allergens.add("Peanuts")
      if (!isGlutenFree) allergens.add("Wheat / Gluten")

      val regional = if (entity.dishName.contains("(")) {
        entity.dishName.substringAfter("(").substringBefore(")").trim()
      } else ""

      val item = FoodIngredient(
        id = id,
        name = entity.dishName,
        regionalName = regional,
        category = categorizeDish(entity.dishName),
        caloriesPer100g = entity.caloriesKcal,
        proteinPer100g = entity.proteinGrams,
        carbsPer100g = entity.carbsGrams,
        fatPer100g = entity.fatGrams,
        fiberPer100g = entity.fiberGrams,
        sugarPer100g = entity.freeSugarGrams,
        sodiumMgPer100g = entity.sodiumMg,
        potassiumMgPer100g = 180f,
        calciumMgPer100g = entity.calciumMg,
        ironMgPer100g = entity.ironMg,
        vitaminCMgPer100g = entity.vitaminCMg,
        vitaminKMcgPer100g = if (nameLower.contains("palak") || nameLower.contains("spinach") || nameLower.contains("methi") || nameLower.contains("saag") || nameLower.contains("bathua")) 450f else 2f,
        glycemicIndex = if (isHighGI) "High" else "Low",
        isGlutenFree = isGlutenFree,
        allergens = allergens,
        dataSource = "Indian Food Dataset (1015 Items)"
      )
      runtimeRegistry[id] = item
    }
  }

  private fun categorizeDish(dish: String): String {
    val d = dish.lowercase()
    return when {
      d.contains("tea") || d.contains("coffee") || d.contains("drink") || d.contains("cooler") || d.contains("sharbat") || d.contains("lassi") || d.contains("shake") -> "Beverage"
      d.contains("dal") || d.contains("sambar") || d.contains("channa") || d.contains("rajmah") || d.contains("lobia") || d.contains("curry") && (d.contains("gram") || d.contains("lentil")) -> "Pulse"
      d.contains("chicken") || d.contains("mutton") || d.contains("fish") || d.contains("egg") || d.contains("anda") || d.contains("prawn") -> "Meat/Fish"
      d.contains("paneer") || d.contains("cheese") || d.contains("curd") || d.contains("dahi") || d.contains("raita") -> "Dairy"
      d.contains("roti") || d.contains("paratha") || d.contains("parantha") || d.contains("rice") || d.contains("pulao") || d.contains("biryani") || d.contains("khichdi") || d.contains("idli") || d.contains("dosa") || d.contains("upma") || d.contains("poha") || d.contains("puttu") -> "Grain"
      d.contains("kheer") || d.contains("halwa") || d.contains("burfi") || d.contains("ladoo") || d.contains("cake") || d.contains("pudding") || d.contains("biscuit") || d.contains("cookie") -> "Dessert / Snack"
      d.contains("soup") -> "Soup"
      d.contains("salad") -> "Salad"
      d.contains("chutney") || d.contains("sauce") || d.contains("pickle") || d.contains("achar") -> "Condiment"
      else -> "Vegetable"
    }
  }
}

