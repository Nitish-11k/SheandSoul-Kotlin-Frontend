package com.example.sheandsoul_nick.data.remote

data class NextMenstrualResponse(
    val nextPeriodStartDate: String,
    val nextPeriodEndDate: String,
    val nextOvulationDate: String,
    val nextOvulationEndDate: String,
    val nextFertileWindowStartDate: String,
    val nextFertileWindowEndDate: String,
    val nextFollicularStartDate: String,
    val nextFollicularEndDate: String,
    val nextLutealStartDate: String,
    val nextLutealEndDate: String
)