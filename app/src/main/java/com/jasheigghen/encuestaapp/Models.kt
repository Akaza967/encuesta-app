package com.jasheigghen.encuestaapp

data class Survey(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val active: Boolean = true,
    val category: String = "General",
    val description: String = ""
)

data class Category(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class Vote(
    val surveyId: String = "",
    val selectedOptionIndex: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)

data class SurveyResult(
    val option: String,
    val count: Int
)
