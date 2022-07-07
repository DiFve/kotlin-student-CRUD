package com.example.blog.DTO

import com.example.blog.model.Student
import java.time.LocalDate
import java.time.LocalDateTime

data class StudentDTO (
    var firstName: String,
    var lastName: String,
    var age: Int,
    var score: Int,
    var birthDate: LocalDate,
    var grade: Double? = 0.0,

){
    companion object {
        fun toStudent(studentDTO: StudentDTO): Student = Student(
            firstName = studentDTO.firstName,
            lastName = studentDTO.lastName,
            age = studentDTO.age,
            score = studentDTO.score,
            birthDate = studentDTO.birthDate,
            createDate = LocalDateTime.now(),
            updateDate = LocalDateTime.now()
        )


    }
}