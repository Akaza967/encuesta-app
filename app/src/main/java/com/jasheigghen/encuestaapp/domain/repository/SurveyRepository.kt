package com.jasheigghen.encuestaapp.domain.repository

import com.jasheigghen.encuestaapp.domain.model.Survey
import com.jasheigghen.encuestaapp.domain.model.Vote

interface SurveyRepository {
    suspend fun getActiveSurveys(): List<Survey>
    suspend fun submitVote(vote: Vote)
    suspend fun createSurvey(survey: Survey)
    suspend fun getResults(surveyId: String): Map<Int, Int>
}
