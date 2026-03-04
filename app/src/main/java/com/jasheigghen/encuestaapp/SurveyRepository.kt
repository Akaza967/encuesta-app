package com.jasheigghen.encuestaapp

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SurveyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val surveysCollection = firestore.collection("surveys")
    private val votesCollection = firestore.collection("votes")

    suspend fun getActiveSurveys(): List<Survey> {
        return try {
            surveysCollection.whereEqualTo("active", true)
                .get()
                .await()
                .toObjects(Survey::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitVote(vote: Vote) {
        try {
            votesCollection.add(vote).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun createSurvey(survey: Survey) {
        try {
            val docRef = surveysCollection.document()
            surveysCollection.document(docRef.id).set(survey.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getResults(surveyId: String): Map<Int, Int> {
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
