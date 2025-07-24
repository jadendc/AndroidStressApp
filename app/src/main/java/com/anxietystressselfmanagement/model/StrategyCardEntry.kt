package com.anxietystressselfmanagement.model

data class StrategyCardEntry(
    val date: String,
    val strategy: String,
    val action: String,
    val rating: Int? = null
)
