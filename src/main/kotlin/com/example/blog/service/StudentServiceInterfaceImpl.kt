package com.example.blog.service

import com.example.blog.DTO.StudentDTO
import com.example.blog.exception.IDNotFoundException
import com.example.blog.model.Student
import com.example.blog.repo.StudentRepo
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class StudentServiceInterfaceImpl(

    private var studentRepo: StudentRepo,
    private var reportService: ReportServiceInterfaceImpl

    ) : StudentServiceInterface {

    var logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun createStudent(studentData: StudentDTO): StudentDTO {

        val grade = this.gradeCalculate(studentData.score)

        val studentSave:Student = StudentDTO.toStudent(studentData)
        studentSave.grade = grade

        studentRepo.save(studentSave)

        logger.info("Student created")

        return StudentDTO(
            firstName = studentData.firstName,
            lastName = studentData.lastName,
            age = studentData.age,
            score = studentData.score,
            grade = grade,
            birthDate = studentData.birthDate,
        )
    }

    override fun getAllStudent(): List<StudentDTO> {
        val allStudentEntity = studentRepo.findAll()
        return allStudentEntity.map { Student.toStudentDTO(it) }
    }

    override fun getByFirstName(firstName: String): Iterable<Student> {
        return studentRepo.findByFirstName(firstName)
    }

    override fun getAllByGrade(grade:Double): List<Student> {
        this.gradeRefresh()
        return studentRepo.findAllByGrade(grade)

    }

    override fun convertListStudentToString(studentList: List<Student>): MutableList<MutableList<String>> {
        val studentListString:MutableList<MutableList<String>> = mutableListOf()


        studentList.map {
            var studentListConvert:MutableList<String> = mutableListOf()
            studentListConvert.add(it.firstName)
            studentListConvert.add(it.lastName)
            studentListConvert.add(it.score.toString())
            studentListConvert.add(it.grade.toString())
            studentListString.add(studentListConvert)
        }

        return studentListString
    }

    override fun updateStudent(id: Long,score: Int):StudentDTO {
        if(!this.existByID(id)){
            logger.warn("This ID does not exist")
            throw IDNotFoundException()
        }
        var oldStudent = studentRepo.findById(id).get()
        val grade = this.gradeCalculate(score)
        oldStudent.score = score
        oldStudent.grade = grade
        oldStudent.updateDate = LocalDateTime.now()
        studentRepo.save(oldStudent)


        logger.info("Info of $id has been update successfully")
        return Student.toStudentDTO(oldStudent)
    }

    override fun updateScore(option: String, score: Int,id:Long): Int {

        if(!(option=="add" || option=="minus")){
            throw Exception("Option not in format")
        }

        var newScore = score
        if(option == "minus"){
            newScore*=-1
        }


        var oldStudent = studentRepo.findById(id).get()
        oldStudent.score += newScore
        studentRepo.save(oldStudent)

        val student = Student(
            firstName = "Peacefully",
            lastName = "Rifle",
            age = 28,
            grade = 2.0,
            score = 88,
            birthDate = LocalDate.parse("2002-07-21"),
            id = id
        )


        logger.info("Score of $id has been update successfully")
        return oldStudent.score
    }

    override fun deleteStudent(id: Long) {
        if(!this.existByID(id)){
            logger.warn("This ID does not exist")
            throw IDNotFoundException()
        }

        logger.info("Delete student successfully")
        studentRepo.deleteById(id)
    }

    override fun exportStudentXLSX(): ByteArray {
        val columnName: List<String> = listOf(
            "id",
            "age",
            "first_name",
            "last_name",
            "score",
            "grade",
            "birth_date",
            "create_date",
            "update_date"

        )
        var studentData = studentRepo.findAll(Sort.by(Sort.Direction.ASC,"id"))
        var studentDataList: MutableList<MutableList<String>> = mutableListOf()

        studentData.map {
            studentDataList.add(
                    mutableListOf(
                    "${it.id}",
                    "${it.age}",
                    it.firstName,
                    it.lastName,
                    "${it.score}",
                    "${it.grade}",
                    it.birthDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    "${it.createDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))}",
                    "${it.updateDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))}",
                )
            )
        }


        return reportService.convertWorkbookToByte(reportService.exportReportWorkbook(XSSFWorkbook(),studentDataList,columnName,"All Student",0))
    }

    override fun exportStudentByGradePDF(file: MultipartFile, grade: Double): ByteArray {

        if(!(grade == 0.0 || grade== 1.0 || grade==1.5 || grade == 2.0 || grade== 2.5 || grade==3.0 || grade == 3.5 || grade== 4.0)){
            throw Exception("Grade must be in format")
        }

        val wb: Workbook = reportService.importReportFormat(file)
        val studentGradeList = this.getAllByGrade(grade)
        val studentGradeListString = this.convertListStudentToString(studentGradeList)

        val report = reportService.exportReportWorkbook(wb,studentGradeListString, listOf("first_name","last_name","score"),"Grade = $grade",1)

        val reportPDF = reportService.convertWorkBookToPDF(report)

        //val reportXLSX = reportService.convertWorkbookToByte(report)
        return reportPDF
    }

    override fun importStudent(studentList:MutableList<MutableList<String>>):List<StudentDTO> {

        var studentDTOList : MutableList<StudentDTO> = mutableListOf()

        studentList.map {
                var student = StudentDTO(
                age = it[0].toInt(),
                firstName = it[1],
                lastName = it[2],
                score = it[3].toInt(),
                grade = it[4].toDouble(),
                birthDate = LocalDate.parse(
                            LocalDate.parse(it[5], DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                .format(DateTimeFormatter.ISO_LOCAL_DATE),DateTimeFormatter.ISO_LOCAL_DATE
                            )
                )

            studentDTOList.add(student)

            this.createStudent(student)
        }
        return studentDTOList
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

    @Scheduled(cron = "0 * * * * *")
    override fun gradeRefresh() {
        var student = studentRepo.findAll()
        student.map {
            it.grade = this.gradeCalculate(it.score)
            studentRepo.save(it)
        }

        logger.info("Grade refresh")
    }


}