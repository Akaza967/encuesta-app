package com.jasheigghen.encuestaapp.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object UserSurveys : Screen("user_surveys")
    object AdminSurveys : Screen("admin_surveys")
    object CreateSurvey : Screen("create_survey")
    object Results : Screen("results/{surveyId}") {
        fun createRoute(surveyId: String) = "results/$surveyId"
    }
}
