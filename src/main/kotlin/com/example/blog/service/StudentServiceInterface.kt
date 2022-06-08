package com.example.blog.service

import com.example.blog.DTO.StudentDTO
import com.example.blog.model.Student

interface StudentServiceInterface {
    fun createStudent(studentData: StudentDTO): StudentDTO
    fun getAllStudent():List<StudentDTO>
    fun getByFirstName(firstName:String):Iterable<Student>
    fun updateStudent(id: Long, studentData: StudentDTO):StudentDTO
    fun deleteStudent(id: Long)
    fun gradeCalculate(score: Int): Double
    fun existByID(id: Long):Boolean
}