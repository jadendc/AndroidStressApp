package com.anxietystressselfmanagement.model

import kotlinx.serialization.Serializable
/*
    Saves a detailed description of each action, future purpose
 */
@Serializable
data class ActionDescription(
    val action: String,
    val details: String
)
