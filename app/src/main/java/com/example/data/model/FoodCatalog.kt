package com.example.data.model

data class FoodItem(
    val id: String,
    val name: String,
    val baseCalories: Float,
    val baseProtein: Float,
    val baseCarbs: Float,
    val baseFats: Float,
    val baseFiber: Float,
    val baseQuantityVal: Float,
    val baseQuantityUnit: String, // "piece", "g", "ml", "katori/cup"
    val category: String, // "Grains", "Lentils", "Dairy", "Poultry/Eggs", "Snacks", "Vegetables", "South Indian"
    val suggestedQuantities: List<Float>
)

object FoodCatalog {
    val items = listOf(
        // Roti
        FoodItem(
            id = "roti",
            name = "Roti (Plain)",
            baseCalories = 85f,
            baseProtein = 3.0f,
            baseCarbs = 18.0f,
            baseFats = 0.5f,
            baseFiber = 2.5f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "Grains",
            suggestedQuantities = listOf(1f, 1.5f, 2f, 2.5f, 3f, 4f)
        ),
        FoodItem(
            id = "paratha",
            name = "Aloo Paratha",
            baseCalories = 210f,
            baseProtein = 4.5f,
            baseCarbs = 32.0f,
            baseFats = 7.5f,
            baseFiber = 3.5f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "Grains",
            suggestedQuantities = listOf(1f, 1.5f, 2f, 2.5f, 3f)
        ),
        // Rice
        FoodItem(
            id = "rice",
            name = "Basmati Rice (Cooked)",
            baseCalories = 130f, // per 100g
            baseProtein = 2.7f,
            baseCarbs = 28.0f,
            baseFats = 0.3f,
            baseFiber = 0.4f,
            baseQuantityVal = 100f,
            baseQuantityUnit = "g",
            category = "Grains",
            suggestedQuantities = listOf(50f, 100f, 150f, 200f, 250f, 300f)
        ),
        // Dal
        FoodItem(
            id = "dal",
            name = "Yellow Dal (Tadka)",
            baseCalories = 150f,
            baseProtein = 7.0f,
            baseCarbs = 22.0f,
            baseFats = 4.0f,
            baseFiber = 5.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "katori/cup",
            category = "Lentils",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f, 2.5f)
        ),
        FoodItem(
            id = "dal_makhani",
            name = "Dal Makhani",
            baseCalories = 230f,
            baseProtein = 8.5f,
            baseCarbs = 24.0f,
            baseFats = 11.0f,
            baseFiber = 6.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "katori/cup",
            category = "Lentils",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f, 2.5f)
        ),
        // Paneer
        FoodItem(
            id = "paneer",
            name = "Paneer Bhurji",
            baseCalories = 190f,
            baseProtein = 11.5f,
            baseCarbs = 4.0f,
            baseFats = 14.0f,
            baseFiber = 1.0f,
            baseQuantityVal = 100f,
            baseQuantityUnit = "g",
            category = "Dairy",
            suggestedQuantities = listOf(50f, 100f, 150f, 200f, 250f)
        ),
        FoodItem(
            id = "paneer_cubes",
            name = "Paneer Raw (Cubes)",
            baseCalories = 265f,
            baseProtein = 18.0f,
            baseCarbs = 1.5f,
            baseFats = 20.0f,
            baseFiber = 0.0f,
            baseQuantityVal = 100f,
            baseQuantityUnit = "g",
            category = "Dairy",
            suggestedQuantities = listOf(50f, 100f, 150f, 200f)
        ),
        // South Indian
        FoodItem(
            id = "dosa",
            name = "Plain Dosa",
            baseCalories = 135f,
            baseProtein = 3.0f,
            baseCarbs = 29.0f,
            baseFats = 1.0f,
            baseFiber = 1.2f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "South Indian",
            suggestedQuantities = listOf(1f, 1.5f, 2f, 2.5f, 3f)
        ),
        FoodItem(
            id = "idli",
            name = "Idli",
            baseCalories = 60f,
            baseProtein = 1.5f,
            baseCarbs = 13.0f,
            baseFats = 0.2f,
            baseFiber = 0.8f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "South Indian",
            suggestedQuantities = listOf(1f, 2f, 3f, 4f, 5f)
        ),
        FoodItem(
            id = "sambar",
            name = "Sambar",
            baseCalories = 90f,
            baseProtein = 3.2f,
            baseCarbs = 14.0f,
            baseFats = 2.2f,
            baseFiber = 3.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "katori/cup",
            category = "South Indian",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        // Breakfast grains
        FoodItem(
            id = "poha",
            name = "Poha (Cooked)",
            baseCalories = 180f,
            baseProtein = 3.5f,
            baseCarbs = 35.0f,
            baseFats = 2.5f,
            baseFiber = 1.8f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "plate/cup",
            category = "Grains",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        FoodItem(
            id = "upma",
            name = "Upma (Semolina)",
            baseCalories = 195f,
            baseProtein = 4.2f,
            baseCarbs = 37.0f,
            baseFats = 3.5f,
            baseFiber = 2.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "plate/cup",
            category = "Grains",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        // Eggs & Poultry
        FoodItem(
            id = "boiled_egg",
            name = "Boiled Egg",
            baseCalories = 75f,
            baseProtein = 6.3f,
            baseCarbs = 0.6f,
            baseFats = 5.0f,
            baseFiber = 0.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "Poultry/Eggs",
            suggestedQuantities = listOf(1f, 2f, 3f, 4f)
        ),
        FoodItem(
            id = "egg_omlette",
            name = "Egg Omelette (Double Egg)",
            baseCalories = 175f,
            baseProtein = 12.5f,
            baseCarbs = 1.2f,
            baseFats = 13.0f,
            baseFiber = 0.2f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "plate",
            category = "Poultry/Eggs",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        FoodItem(
            id = "chicken_curry",
            name = "Chicken Curry (Standard)",
            baseCalories = 220f,
            baseProtein = 22.0f,
            baseCarbs = 6.0f,
            baseFats = 12.0f,
            baseFiber = 1.5f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "katori/cup",
            category = "Poultry/Eggs",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        // Dairy & drinks
        FoodItem(
            id = "milk",
            name = "Whole Milk (Cow/Buffalo)",
            baseCalories = 65f, // per 100ml
            baseProtein = 3.2f,
            baseCarbs = 4.7f,
            baseFats = 3.6f,
            baseFiber = 0.0f,
            baseQuantityVal = 100f,
            baseQuantityUnit = "ml",
            category = "Dairy",
            suggestedQuantities = listOf(100f, 150f, 200f, 250f, 300f, 500f)
        ),
        FoodItem(
            id = "curd",
            name = "Plain Curd / Dahi",
            baseCalories = 60f, // per 100g
            baseProtein = 3.1f,
            baseCarbs = 4.3f,
            baseFats = 3.2f,
            baseFiber = 0.0f,
            baseQuantityVal = 100f,
            baseQuantityUnit = "g",
            category = "Dairy",
            suggestedQuantities = listOf(50f, 100f, 150f, 200f, 250f, 300f)
        ),
        // Veggies/Salad
        FoodItem(
            id = "salad",
            name = "Green Salad (Cucumber, Tomato)",
            baseCalories = 25f,
            baseProtein = 0.8f,
            baseCarbs = 4.5f,
            baseFats = 0.1f,
            baseFiber = 1.8f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "bowl",
            category = "Vegetables",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        FoodItem(
            id = "mix_veg",
            name = "Mix Veg Sabzi",
            baseCalories = 110f,
            baseProtein = 2.5f,
            baseCarbs = 14.0f,
            baseFats = 5.0f,
            baseFiber = 3.5f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "katori/cup",
            category = "Vegetables",
            suggestedQuantities = listOf(0.5f, 1f, 1.5f, 2f)
        ),
        // Snacks
        FoodItem(
            id = "samosa",
            name = "Samosa (Medium)",
            baseCalories = 250f,
            baseProtein = 4.0f,
            baseCarbs = 32.0f,
            baseFats = 12.0f,
            baseFiber = 2.5f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "piece(s)",
            category = "Snacks",
            suggestedQuantities = listOf(1f, 2f, 3f)
        ),
        FoodItem(
            id = "chai",
            name = "Masala Tea (with Sugar & Milk)",
            baseCalories = 90f,
            baseProtein = 1.8f,
            baseCarbs = 14.0f,
            baseFats = 2.5f,
            baseFiber = 0.0f,
            baseQuantityVal = 1f,
            baseQuantityUnit = "cup",
            category = "Dairy",
            suggestedQuantities = listOf(1f, 1.5f, 2f)
        )
    )

    fun search(query: String): List<FoodItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return items
        return items.filter {
            it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
        }
    }
}
