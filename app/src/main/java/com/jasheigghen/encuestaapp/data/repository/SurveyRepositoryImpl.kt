package com.jasheigghen.encuestaapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jasheigghen.encuestaapp.domain.model.Survey
import com.jasheigghen.encuestaapp.domain.model.Vote
import com.jasheigghen.encuestaapp.domain.repository.SurveyRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SurveyRepository {

    private val surveysCollection = firestore.collection("surveys")
    private val votesCollection = firestore.collection("votes")

    override suspend fun getActiveSurveys(): List<Survey> {
        return try {
            surveysCollection.whereEqualTo("active", true)
                .get()
                .await()
                .toObjects(Survey::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun submitVote(vote: Vote) {
        try {
            votesCollection.add(vote).await()
        } catch (e: Exception) {
            // Log error
        }
    }

    override suspend fun createSurvey(survey: Survey) {
        try {
            val docRef = surveysCollection.document()
            surveysCollection.document(docRef.id).set(survey.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // Log error
        }
    }

    override suspend fun getResults(surveyId: String): Map<Int, Int> {
        return try {
            val votes = votesCollection.whereEqualTo("surveyId", surveyId)
                .get()
                .await()
            
            val results = mutableMapOf<Int, Int>()
            votes.forEach { doc ->
                val optionIndex = doc.getLong("selectedOptionIndex")?.toInt() ?: -1
                if (optionIndex != -1) {
                    results[optionIndex] = results.getOrDefault(optionIndex, 0) + 1
                }
            }
            results
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
