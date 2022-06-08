package com.example.blog.service

import com.example.blog.DTO.StudentDTO
import com.example.blog.model.Student
import com.example.blog.repo.StudentRepo
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudentServiceInterfaceImpl(

    private var studentRepo: StudentRepo

    ) : StudentServiceInterface {
    override fun createStudent(studentData: StudentDTO): StudentDTO {

        val grade = this.gradeCalculate(studentData.score)

        val studentSave = Student(
            firstName = studentData.firstName,
            lastName = studentData.lastName,
            age = studentData.age,
            score = studentData.score,
            grade = grade,
            birthDate = studentData.birthDate,
            createDate = LocalDateTime.now(),
            updateDate = LocalDateTime.now(),
        )
        studentRepo.save(studentSave)

        return StudentDTO(
            firstName = studentData.firstName,
            lastName = studentData.lastName,
            age = studentData.age,
            score = studentData.score,
            grade = grade,
            birthDate = studentData.birthDate,
        )
    }

    override fun getAllStudent():List<StudentDTO> {
        val lastIndex = studentRepo.findAll().lastIndex
        var allStudentDTO: MutableList<StudentDTO> = arrayListOf()

        for(item in 0 until lastIndex){

            var studentData = studentRepo.findAll()[item]

            allStudentDTO.add(
                StudentDTO(
                    firstName = studentData.firstName,
                    lastName = studentData.lastName,
                    age = studentData.age,
                    score = studentData.score,
                    grade = studentData.grade,
                    birthDate = studentData.birthDate,
                )
            )
        }
        return allStudentDTO
    }

    override fun getByFirstName(firstName: String): Iterable<Student> {
        return studentRepo.findByFirstName(firstName)
    }

    override fun updateStudent(id: Long,newStudentData: StudentDTO):StudentDTO {
        if(!this.existByID(id)){
            throw Exception("This id does not exist")
        }
        val createDate:LocalDateTime = studentRepo.findById(id).get().createDate
        val grade = this.gradeCalculate(newStudentData.score)
        val studentSave = Student(
            firstName = newStudentData.firstName,
            lastName = newStudentData.lastName,
            age = newStudentData.age,
            score = newStudentData.score,
            grade = grade,
            birthDate = newStudentData.birthDate,
            createDate = createDate,
            updateDate = LocalDateTime.now(),
            id = id
        )
        studentRepo.save(studentSave)
        return StudentDTO(
            firstName = newStudentData.firstName,
            lastName = newStudentData.lastName,
            age = newStudentData.age,
            score = newStudentData.score,
            grade = grade,
            birthDate = newStudentData.birthDate,
        )
    }

    override fun deleteStudent(id: Long) {
        if(!this.existByID(id)){
            throw Exception("This id does not exist")
        }

        studentRepo.deleteById(id)
    }

    override fun gradeCalculate(score: Int): Double {
        var grade: Double = 0.0

        if (score in 80..100) {
            grade = 4.0
        } else if (score in 75..79) {
            grade = 3.5
        } else if (score in 70..74) {
            grade = 3.0
        } else if (score in 65..69) {
            grade = 2.5
        } else if (score in 60..64) {
            grade = 2.0
        } else if (score in 55..59) {
            grade = 1.5
        } else if (score in 50..54) {
            grade = 1.0
        } else if (score in 0..49) {
            grade = 0.0
        } else {
            throw Exception("Score range should be between 0-100")
        }
        return grade
    }

    override fun existByID(id: Long): Boolean {
        return studentRepo.existsById(id)
    }


}