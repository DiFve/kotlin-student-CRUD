package com.example.blog.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType

@Entity
class Student(
    var firstName: String,
    val lastName: String,
    val age: Int,
    val score: Int,
    val grade: Double? = null,
    val birthDate: LocalDate,
    val updateDate: LocalDateTime,
    val createDate:LocalDateTime,
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = -1) {

//    private constructor() : this("","",0,0,0.0,LocalDateTime.now(), LocalDateTime.now())
}
