package com.anxietystressselfmanagement.model

import kotlinx.serialization.Serializable

/*
    Used for storing and saving data for Strategies and Actions
 */
@Serializable
data class StrategyAction(
    val strategy: String,
    val action : String,
)
