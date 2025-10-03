package com.example.sheandsoul_nick.features.auth.presentation

data class Question(val id: Int, val text: String, val answerType: AnswerType)
enum class AnswerType { NUMBER, YES_NO }
fun QuizModels() {
//
//    data class Question(val id: Int, val text: String, val answerType: AnswerType)
//    enum class AnswerType { NUMBER, YES_NO }
}