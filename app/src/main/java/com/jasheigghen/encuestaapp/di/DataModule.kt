package com.jasheigghen.encuestaapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jasheigghen.encuestaapp.data.repository.AuthRepositoryImpl
import com.jasheigghen.encuestaapp.data.repository.SurveyRepositoryImpl
import com.jasheigghen.encuestaapp.domain.repository.AuthRepository
import com.jasheigghen.encuestaapp.domain.repository.SurveyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideSurveyRepository(
        firestore: FirebaseFirestore
    ): SurveyRepository = SurveyRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(auth)
}
