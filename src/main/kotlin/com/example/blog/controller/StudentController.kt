package com.example.blog.controller

import com.example.blog.DTO.StudentDTO
import com.example.blog.model.Student
import com.example.blog.service.StudentServiceInterfaceImpl
import org.springframework.web.bind.annotation.*

@RestController
class StudentController(
    private val studentServiceInterfaceImpl: StudentServiceInterfaceImpl
) {

    @PostMapping("/student")
    fun createStudent(@RequestBody studentData: StudentDTO): StudentDTO {
        return studentServiceInterfaceImpl.createStudent(studentData)
    }

    @GetMapping("/getAll")
    fun getAllStudent():List<StudentDTO> {
        return studentServiceInterfaceImpl.getAllStudent()
    }

    @GetMapping("/getByFirstName/{firstName}")
    fun getByFirstName(@PathVariable firstName:String): Iterable<Student> {
        return studentServiceInterfaceImpl.getByFirstName(firstName)
    }

    @PostMapping("/update/{id}")
    fun updateStudent(@PathVariable id: Long, @RequestBody newStudentData: StudentDTO): StudentDTO{
        return studentServiceInterfaceImpl.updateStudent(id,newStudentData)
    }

    @PostMapping("/delete/{id}")
    fun deleteStudent(@PathVariable id: Long){
        studentServiceInterfaceImpl.deleteStudent(id)
    }
}