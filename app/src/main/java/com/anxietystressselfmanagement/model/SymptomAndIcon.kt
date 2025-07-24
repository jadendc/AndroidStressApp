package com.anxietystressselfmanagement.model

import kotlinx.serialization.Serializable

@Serializable
data class SymptomAndIcon(
    val symptoms: List<String>,
    val icon: String
)
