package com.example.blog.controller

import com.example.blog.DTO.StudentDTO
import com.example.blog.model.Student
import com.example.blog.service.ReportServiceInterfaceImpl
import com.example.blog.service.StudentServiceInterfaceImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/student")
class StudentController(
    private val studentService: StudentServiceInterfaceImpl,
    private val reportService: ReportServiceInterfaceImpl
) {
    var logger: Logger = LoggerFactory.getLogger(this::class.java)
    @PostMapping("")
    fun createStudent(@RequestBody studentData: StudentDTO): StudentDTO {
        return studentService.createStudent(studentData)
    }

    @GetMapping("")
    fun getAllStudent():List<StudentDTO> {
        return studentService.getAllStudent()
    }

    @GetMapping("/firstName/{firstName}")
    fun getByFirstName(@PathVariable firstName:String): Iterable<Student> {
        return studentService.getByFirstName(firstName)
    }

    @PutMapping("/{id}/{score}")
    fun updateStudent(@PathVariable id: Long, @PathVariable score: Int): StudentDTO{
        return studentService.updateStudent(id,score)
    }

    @DeleteMapping("/{id}")
    fun deleteStudent(@PathVariable id: Long){
        studentService.deleteStudent(id)
    }

    @PostMapping("/addScore/{id}")
    fun updateScore(@PathVariable id:Long,@RequestParam option:String, @RequestParam score:Int): Int{
        return studentService.updateScore(option,score,id)
    }

    @PostMapping("/export/student-excel")
    fun exportStudentXLSX(): ResponseEntity<ByteArray> {
        val report = studentService.exportStudentXLSX()

        return reportService.createResponseEntity(
            report,
            "student_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss"))}.xlsx"
        )
    }

    @PostMapping("/import/student-excel")
    fun importStudentXLSX(@RequestParam("file") file: MultipartFile): List<StudentDTO>{
        val listStudent = reportService.importReport(file = file)
        return studentService.importStudent(listStudent)
    }

    @PostMapping("/export/student-pdf/grade/{grade}")
    fun exportStudentByGradePDF(@PathVariable grade:Double): ResponseEntity<ByteArray>{

        var file = File("./form/Report_Form.xlsx")
        var input = FileInputStream(file)
        var multipartFile = MockMultipartFile("Report_Form.xlsx","Report_Form.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",input)


        val report = studentService.exportStudentByGradePDF(multipartFile,grade)

        return reportService.createResponseEntity(
            report,
            "student_grade_${grade}_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss"))}.pdf"
        )
    }



}