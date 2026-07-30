package com.example.di

import com.example.data.DSPRepositoryImpl
import com.example.repository.DSPRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Clean Architecture Dependency Injection container / Service Locator.
 * Provides singleton scopes and repository instances.
 */
object AppContainer {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val dspRepository: DSPRepository by lazy {
        DSPRepositoryImpl(applicationScope)
    }
}
