package com.example.blog.service

import com.example.blog.DTO.StudentDTO
import com.example.blog.model.Student
import org.springframework.web.multipart.MultipartFile

interface StudentServiceInterface {
    fun createStudent(studentData: StudentDTO): StudentDTO
    fun getAllStudent():List<StudentDTO>
    fun getByFirstName(firstName:String):Iterable<Student>
    fun getAllByGrade(grade:Double):List<Student>
    fun convertListStudentToString(studentList:List<Student>):MutableList<MutableList<String>>
    fun updateStudent(id: Long, score: Int):StudentDTO
    fun updateScore(option: String, score:Int,id:Long): Int
    fun deleteStudent(id: Long)
    fun exportStudentXLSX(): ByteArray
    fun exportStudentByGradePDF(file:MultipartFile, grade:Double):ByteArray
    fun importStudent(studentList:MutableList<MutableList<String>>): List<StudentDTO>
    fun gradeCalculate(score: Int): Double
    fun existByID(id: Long):Boolean
    fun gradeRefresh()
}