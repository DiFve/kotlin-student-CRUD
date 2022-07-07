package com.example.blog.controller

import com.example.blog.DTO.StudentDTO
import com.example.blog.exception.IDNotFoundException
import com.example.blog.model.Student
import com.example.blog.service.ReportServiceInterfaceImpl
import com.example.blog.service.StudentServiceInterfaceImpl
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import io.mockk.*
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.tomcat.util.file.Matcher
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class StudentControllerTest(

) {
    private val studentService = mockk<StudentServiceInterfaceImpl>()
    private val reportService = mockk<ReportServiceInterfaceImpl>()
    private val studentController = spyk(StudentController(studentService,reportService))

    @Nested
    @DisplayName("getAllStudent()")
    inner class GetAllStudent() {
        @Test
        fun `should return list of all student`() {
            var student:List<StudentDTO> = listOf(
                StudentDTO(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    score = 81,
                    grade = 4.0,
                    age = 19,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                StudentDTO(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    score = 81,
                    grade = 4.0,
                    age = 19,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )

            every { studentService.getAllStudent() } returns student

            studentController.getAllStudent()

            verify(exactly = 1) { studentService.getAllStudent() }
        }

        @Test
        fun `should return empty list of student`() {
            var student:List<StudentDTO> = listOf()

            every { studentService.getAllStudent() } returns student

            studentController.getAllStudent()

            verify { studentService.getAllStudent() }
        }
    }

    @Nested
    @DisplayName("createStudent()")
    inner class CreateStudent() {
        @Test
        fun `should create student`() {
            var student:StudentDTO = StudentDTO(
                firstName = "Lorem",
                lastName = "Ipsum",
                score = 81,
                grade = 4.0,
                age = 19,
                birthDate = LocalDate.parse("2002-07-21")
            )

            every { studentService.createStudent(student) } returns student

            val expected = studentController.createStudent(student)
            verify(exactly = 1) { studentController.createStudent(student) }
            verify(exactly = 1) { studentController.createStudent(any()) }

            assertEquals(expected,student)
        }
    }

    @Nested
    @DisplayName("updateStudent()")
    inner class UpdateStudent() {
        @Test
        fun `should update student score`() {
            val id:Long = 666
            val score = 98

            var student:StudentDTO = StudentDTO(
                firstName = "Lorem",
                lastName = "Ipsum",
                score = 98,
                grade = 4.0,
                age = 19,
                birthDate = LocalDate.parse("2002-07-21")
            )

            every { studentService.updateStudent( any(),any() ) } returns student

            val expected = studentController.updateStudent(id,score)

            verify(exactly = 1) { studentService.updateStudent(any(),any()) }

            assertEquals(expected,student)
        }

        @Test
        fun `should throw IDNotFoundException`() {
            val id:Long=123
            val score=15

            every { studentService.existByID(id) } returns false

            assertThrows<Exception> { studentController.updateStudent(id,score) }
        }
    }

    @Nested
    @DisplayName("deleteStudent()")
    inner class DeleteStudent() {
        @Test
        fun `should delete student`() {
            val id:Long = 666
            every { studentService.deleteStudent( any() ) } returns Unit

            studentController.deleteStudent(id)
            verify { studentService.deleteStudent(any()) }
        }

        @Test
        fun `should throw IDNotFoundException`() {
            val id:Long = 500
            every { studentService.existByID(id) } returns false
            assertThrows<Exception> { studentController.deleteStudent(id) }
        }
    }

    @Nested
    @DisplayName("updateScore()")
    inner class UpdateScore(){
        @Test
        fun `should add student score`() {
            val id:Long = 666
            val option:String = "add"


            var studentSave:Student = Student(
                firstName = "Lorem",
                lastName = "Ipsum",
                score = 90,
                grade = 4.0,
                age = 19,
                birthDate = LocalDate.parse("2002-07-21")
            )

            every { studentService.updateScore("add",10,any()) } returns studentSave.score

            val expected = studentController.updateScore(id,option,10)

            verify { studentService.updateScore("add",10,any()) }

            assertEquals(expected,studentSave.score)
        }

        @Test
        fun `should reduce student score`() {
            val id:Long = 666
            val option:String = "minus"


            var studentSave:Student = Student(
                firstName = "Lorem",
                lastName = "Ipsum",
                score = 70,
                grade = 4.0,
                age = 19,
                birthDate = LocalDate.parse("2002-07-21")
            )

            every { studentService.updateScore("minus",10,any()) } returns studentSave.score

            val expected = studentController.updateScore(id,option,10)

            verify { studentService.updateScore("minus",10,any()) }

            assertEquals(expected,studentSave.score)
        }

        @Test
        fun `should throw OptionException`() {
            val option="asdsadasd"
            val id:Long=444
            val score = 55
            assertThrows<Exception> { studentController.updateScore(id,option,score) }
        }
    }

    @Nested
    @DisplayName("importStudentXLSX")
    inner class ImportStudentXLSX() {
        @Test
        fun `should import student xlsx to list`() {
            var studentString: MutableList<MutableList<String>> = mutableListOf(
                mutableListOf("Lorem","Ipsum","75","3.5","19","2002-07-21"),
                mutableListOf("Peacefully","Rifle","77","3.5","19","2002-05-23")
            )

            var studentList: List<StudentDTO> = listOf(
                StudentDTO(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    age = 19,
                    grade = 3.5,
                    score = 75,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                StudentDTO(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    age = 19,
                    grade = 3.5,
                    score = 77,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )
            every { reportService.importReport(any()) } returns studentString
            every { studentService.importStudent(any()) } returns studentList

            var file = File("./form/Report_Form.xlsx")
            var input = FileInputStream(file)
            var multipartFile = MockMultipartFile("Report_Form.xlsx","Report_Form.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",input)

            studentController.importStudentXLSX(multipartFile)

            verify(exactly = 1) { reportService.importReport(multipartFile) }
            verify(exactly = 1) { studentService.importStudent(studentString) }
        }
    }

    @Nested
    @DisplayName("exportStudentByGradePDF")
    inner class ExportStudentByGradePDF() {
        @Test
        fun `should exportStudentByGrade as PDF`() {
            val grade = 3.5


            val file = File("./form/Report_Form.xlsx")
            val input = FileInputStream(file)
            val multipartFile = MockMultipartFile("Report_Form.xlsx","Report_Form.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",input)

            val wb:Workbook = XSSFWorkbook()


            val out = ByteArrayOutputStream()
            val document = Document()
            PdfWriter.getInstance(document, out)
            val byteArray = out.toByteArray()
            val fileName = "student_grade_${grade}_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss"))}.pdf"
            val responseEntity: ResponseEntity<ByteArray> = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(byteArray)

            every { studentService.exportStudentByGradePDF(any(),grade) } returns byteArray
            every { reportService.createResponseEntity(byteArray,any()) } returns responseEntity

            studentController.exportStudentByGradePDF(grade)

            verify(exactly = 1) { studentService.exportStudentByGradePDF(any(),grade) }
            verify(exactly = 1) { reportService.createResponseEntity(byteArray,any()) }
        }
    }

    @Nested
    @DisplayName("exportStudentXLSX()")
    inner class ExportStudentXLSX() {
        @Test
        fun `should export student as XLSX`() {
            val wb:Workbook = XSSFWorkbook()
            val out = ByteArrayOutputStream()
            wb.write(out)
            out.close()
            wb.close()
            val byteArray = out.toByteArray()

            val currentTime = LocalDateTime.now()

            val fileName = "student_report_${currentTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss"))}.xlsx"

            val responseEntity: ResponseEntity<ByteArray> = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(byteArray)

            every { studentService.exportStudentXLSX() } returns byteArray
            every { reportService.createResponseEntity(any(), fileName)} returns responseEntity
            every { reportService.convertWorkbookToByte(any()) } returns byteArray
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns currentTime

            studentController.exportStudentXLSX()

            verify(exactly = 1) { studentService.exportStudentXLSX() }
            verify(exactly = 1) { reportService.createResponseEntity(any(),fileName) }

        }
    }
}