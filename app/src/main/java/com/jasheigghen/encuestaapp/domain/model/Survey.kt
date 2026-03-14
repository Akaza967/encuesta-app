package com.jasheigghen.encuestaapp.domain.model

data class Survey(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val active: Boolean = true
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
