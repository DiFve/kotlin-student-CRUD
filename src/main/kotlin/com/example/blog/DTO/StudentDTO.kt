package com.example.blog.DTO

import java.time.LocalDate

data class StudentDTO (
    val firstName: String,
    val lastName: String,
    val age: Int,
    val score: Int,
    val birthDate: LocalDate,
    val grade: Double? = null,
)