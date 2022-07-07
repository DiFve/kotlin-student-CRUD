package com.example.blog.model

import com.example.blog.DTO.StudentDTO
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Table

@Entity
@Table(name = "Student")
data class Student(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = -1,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "last_name")
    val lastName: String,

    @Column(name = "age")
    val age: Int,

    @Column(name = "score")
    var score: Int,

    @Column(name = "grade")
    var grade: Double? = 0.0,

    @Column(name = "birth_date")
    val birthDate: LocalDate,

    @UpdateTimestamp
    @Column(name = "updateDate")
    var updateDate: LocalDateTime? = LocalDateTime.now(),

    @CreationTimestamp
    @Column(name = "createDate")
    var createDate:LocalDateTime? = LocalDateTime.now() , )
{
    companion object {
        fun toStudentDTO(student: Student): StudentDTO = StudentDTO(
            firstName = student.firstName,
            lastName = student.lastName,
            age = student.age,
            score = student.score,
            grade = student.grade,
            birthDate = student.birthDate,
        )


    }
}
