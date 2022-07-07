package com.example.blog.service

import com.example.blog.DTO.StudentDTO
import com.example.blog.exception.IDNotFoundException
import com.example.blog.model.Student
import com.example.blog.repo.StudentRepo
import io.mockk.*
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class StudentServiceTest {

    private val studentRepo = mockk<StudentRepo>()
    private val reportService = mockk<ReportServiceInterfaceImpl>()
    private val studentService = spyk(StudentServiceInterfaceImpl(studentRepo,reportService))


    @Nested
    @DisplayName("getAllStudent()")
    inner class GetAllStudent() {
        @Test
        fun `should return list of all student`() {
            val student:MutableList<Student> = mutableListOf(
                Student(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    score = 81,
                    grade = 4.0,
                    age = 19,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    score = 81,
                    grade = 4.0,
                    age = 19,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )

            every { studentRepo.findAll() } returns student
            val response:List<StudentDTO> = studentService.getAllStudent()
            val expected = student.map { Student.toStudentDTO(it) }
            assertEquals(expected,response)
        }

        @Test
        fun `should return empty list`() {
            val list: MutableList<Student> = mutableListOf()
            every { studentRepo.findAll() } returns list
            val actual = studentService.getAllStudent()
            val expected = list.map { Student.toStudentDTO(it) }
            assertEquals(expected,actual)
        }
    }

    @Nested
    @DisplayName("getAllByGrade()")
    inner class GetAllByGrade() {
        @Test
        fun `should return all student with searched grade`() {
            val grade = 2.0

            val allStudentList: List<Student> = listOf(
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    age = 28,
                    score = 63,
                    grade = 2.0,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                Student(
                    firstName = "Optic",
                    lastName = "Yayy",
                    age = 28,
                    score = 73,
                    grade = 3.0,
                    birthDate = LocalDate.parse("2002-07-21")
                )
            )

            val expected = listOf(allStudentList[0])

            every { studentRepo.findAllByGrade(2.0) } returns expected

            val actual = studentRepo.findAllByGrade(grade)
            assertEquals(expected, actual)
        }

        @Test
        fun `should return empty list when no student with grade`() {

            val grade = 4.0
            val emptyList = listOf<Student>()
            every { studentRepo.findAllByGrade(grade) } returns emptyList

            val actual = studentRepo.findAllByGrade(grade)
            assertEquals(emptyList, actual)
        }
    }

    @Nested
    @DisplayName("createStudent()")
    inner class CreateStudent() {
        @Test
        fun `should save student`() {
            val score = 63
            val grade = 2.0
            val timeNow = LocalDateTime.now()

            val studentDTO = StudentDTO(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                score = score,
                birthDate = LocalDate.parse("2002-07-21")
            )


            val student:Student = StudentDTO.toStudent(studentDTO)
            student.grade = grade
            student.createDate = timeNow
            student.updateDate = timeNow

            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns timeNow
            every { studentRepo.save( student ) } returns student
            every {studentService.gradeCalculate( score )} returns grade
            every {studentService.gradeCalculate( any() )} returns grade

//        every { StudentDTO.toStudent(studentDTO) } returns student


            studentService.createStudent(studentDTO)
            verify(exactly = 1) { studentRepo.save( any() ) }
            verify(exactly = 1) { studentRepo.save( student ) }
        }
    }

    @Nested
    @DisplayName("convertListStudentToString")
    inner class ConvertListStudentToString() {

        @Test
        fun `should return list of string that convert from list of student`() {

            val studentList: List<Student> = listOf(
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    age = 28,
                    score = 63,
                    birthDate = LocalDate.parse("2002-07-21"),
                    grade = 2.0
                ),
                Student(
                    firstName = "VVV",
                    lastName = "Kazatar",
                    age = 22,
                    score = 63,
                    birthDate = LocalDate.parse("2002-07-21"),
                    grade = 2.0
                )
            )

            val expected: MutableList<MutableList<String>> = mutableListOf(
                mutableListOf("Peacefully","Rifle","63","2.0"),
                mutableListOf("VVV","Kazatar","63","2.0"),
            )

            val actual = studentService.convertListStudentToString(studentList)

            assertEquals(expected,actual)

        }

        @Test
        fun `should return empty list of string`() {

            val studentList: List<Student> = listOf()

            val expected: MutableList<MutableList<String>> = mutableListOf()

            val actual = studentService.convertListStudentToString(studentList)

            assertEquals(expected,actual)

        }

    }


    @Nested
    @DisplayName("updateStudent()")
    inner class UpdateStudent(){


        @Test
        fun `should set student score`(){
            val id:Long = 123
            val score = 82
            val grade = 4.0

            val student = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
                id = id
            )

            val studentSave = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                grade = grade,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
                id = id
            )

            val expected = Student.toStudentDTO(studentSave)

            every { studentService.existByID( id ) } returns true
            every { studentService.existByID( any() ) } returns true

            every { studentRepo.findById( id ).get() } returns student
            every { studentRepo.findById( any() ).get() } returns student

            every { studentRepo.save( student ) } returns studentSave

            every { studentService.gradeCalculate( score ) } returns grade
            every { studentService.gradeCalculate( any() ) } returns grade

            val actual = studentService.updateStudent(id,score)

            verify(exactly = 1) { studentRepo.save( student ) }
            verify(exactly = 1) { studentRepo.save( any() ) }

            assertEquals(expected,actual)
        }

        @Test
        fun `should throw IDNotFoundException`() {
            val id:Long = 666
            val score = 58

            every { studentService.existByID(id) } returns false

            assertThrows<IDNotFoundException> { studentService.updateStudent(id,score) }
            verify(exactly = 0) { studentRepo.save(any()) }
            verify(exactly = 0) { studentRepo.findById(id)}
        }

        @Test
        fun `should throw score exception`() {
            val id:Long = 666
            val score = 123

            val student = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
                id = id
            )

            every { studentService.existByID(id) } returns true
            every { studentService.existByID(any()) } returns true

            every { studentRepo.findById(id).get() } returns student
            every { studentRepo.findById(any()).get() } returns student

            assertThrows<Exception> { studentService.updateStudent(id,score) }

            verify(exactly = 0) { studentRepo.save(any()) }
            verify(exactly = 1) { studentRepo.findById(id)}
        }
    }


    @Nested
    @DisplayName("updateScore()")
    inner class UpdateScore() {

        @Test
        fun `should add student score`() {

            val id:Long = 666
            val score = 63
            val adjustScore = 5
            val option = "add"
            val student = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                grade = 2.0,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
            )

            val newScore = score+adjustScore

            val studentSave = student.copy(score = newScore)

            every { studentRepo.findById( any() ).get() } returns student
            every { studentRepo.findById( id ).get() } returns student


            every { studentRepo.save( studentSave ) } returns studentSave
            val actual = studentService.updateScore(option,adjustScore,id)

            verify(exactly = 1) { studentRepo.save(studentSave) }
            verify(exactly = 1) { studentRepo.save( any() ) }
            verify(exactly = 1) { studentRepo.findById(id) }
            verify(exactly = 1) { studentRepo.findById(any()) }
            assertEquals(newScore,actual)
        }

        @Test
        fun `should reduce student score`() {
            val id:Long = 666
            val score = 63
            val adjustScore = 5
            val option = "minus"
            val student = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                grade = 2.0,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
                id = id
            )

            val newScore = score-adjustScore

            val studentSave = student.copy(score = newScore)

            every { studentRepo.findById( any() ).get() } returns student
            every { studentRepo.findById( id ).get() } returns student



            every { studentRepo.save( student ) } returns studentSave
            val actual = studentService.updateScore(option,adjustScore,id)

            verify(exactly = 1) { studentRepo.save(student) }
            verify(exactly = 1) { studentRepo.save( any() ) }
            verify(exactly = 1) { studentRepo.findById(id) }
            verify(exactly = 1) { studentRepo.findById(any()) }

            assertEquals(newScore,actual)
        }

        @Test
        fun `should throw OptionFormatException`() {
            val option="asdfjkl;"
            val score=50
            val id:Long=123

            val student = Student(
                firstName = "Peacefully",
                lastName = "Rifle",
                age = 28,
                grade = 2.0,
                score = score,
                birthDate = LocalDate.parse("2002-07-21"),
                id = id
            )

            assertThrows<Exception> { studentService.updateScore(option,score,id) }

            verify(exactly = 0) { studentRepo.save(student) }
            verify(exactly = 0) { studentRepo.save( any() ) }
            verify(exactly = 0) { studentRepo.findById(id) }
            verify(exactly = 0) { studentRepo.findById(any()) }
        }
    }

    @Nested
    @DisplayName("deleteStudent()")
    inner class DeleteStudent(){
        @Test
        fun `should delete student`() {
            val id:Long = 123
            every { studentService.existByID(id) } returns true
            every { studentService.existByID( any() ) } returns true
            every {studentRepo.deleteById(id)} returns Unit
            studentService.deleteStudent(id)

            verify(exactly = 1) { studentRepo.deleteById( id ) }
            verify(exactly = 1) { studentRepo.deleteById(any()) }
        }

        @Test
        fun `should throw IDNotFoundException`() {
            val id:Long = 123
            every { studentService.existByID(id) } returns false
            every { studentService.existByID( any() ) } returns false

            assertThrows<IDNotFoundException> { studentService.deleteStudent(id) }

            verify(exactly = 0) { studentRepo.deleteById( any() ) }
            verify(exactly = 0) { studentRepo.deleteById( id ) }
        }
    }

    @Nested
    @DisplayName("gradeCalculate()")
    inner class GradeCalculate() {
        @Test
        fun `should calculate grade four`() {
            var score: Int = 80
            var expected:Double = 4.0
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade three point five`() {
            var score: Int = 75
            var expected:Double = 3.5
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade three`() {
            var score: Int = 70
            var expected:Double = 3.0
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade two point five`() {
            var score: Int = 65
            var expected:Double = 2.5
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade two`() {
            var score: Int = 60
            var expected:Double = 2.0
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade one point five`() {
            var score: Int = 55
            var expected:Double = 1.5
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade one`() {
            var score: Int = 50
            var expected:Double = 1.0
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should calculate grade zero`() {
            var score: Int = 0
            var expected:Double = 0.0
            var actual = studentService.gradeCalculate(score)
            assertEquals(expected,actual)
        }

        @Test
        fun `should return ScoreException`() {
            var score: Int = 123
            assertThrows<Exception> { studentService.gradeCalculate(score) }

        }
    }

    @Nested
    @DisplayName("gradeRefresh()")
    inner class GradeRefresh(){
        @Test
        fun `should refresh grade`() {
            var student:List<Student> = listOf(
                Student(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    score = 81,
                    grade = 3.0,
                    age = 19,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    score = 81,
                    grade = 3.5,
                    age = 19,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )

            every { studentRepo.findAll() } returns student
            every { studentRepo.save( student[0] ) } returns student[0]
            every { studentRepo.save( student[1] ) } returns student[1]

            studentService.gradeRefresh()

            verify(exactly = 1) { studentRepo.save(student[0]) }
            verify(exactly = 1) { studentRepo.save(student[1]) }
            verify(exactly = 2) { studentRepo.save(any()) }
        }

    }

    @Nested
    @DisplayName("convertWorkbookToByte()")
    inner class ConvertWorkbookToByte() {
        @Test
        fun `should export xlsx`() {
            val wb:Workbook = XSSFWorkbook()
            val out = ByteArrayOutputStream()
            wb.write(out)
            out.close()
            wb.close()
            val byteArray = out.toByteArray()
            every { reportService.convertWorkbookToByte(any() ) } returns byteArray

            val actual = reportService.convertWorkbookToByte(wb)
            assertEquals(byteArray,actual)
        }
    }

    @Nested
    @DisplayName("exportStudentByGradeXLSX()")
    inner class ExportStudentByGradeXLSX() {
        @Test
        fun `should export student xlsx by grade`() {
            val grade = 3.5

            var student:List<Student> = listOf(
                Student(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    score = 75,
                    grade = grade,
                    age = 19,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    score = 77,
                    grade = grade,
                    age = 19,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )

            var studentString: MutableList<MutableList<String>> = mutableListOf(
                mutableListOf("Lorem","Ipsum","75","3.5","19","2002-07-21"),
                mutableListOf("Peacefully","Rifle","77","3.5","19","2002-05-23")
            )

            var file = File("./form/Report_Form.xlsx")
            var input = FileInputStream(file)
            var multipartFile = MockMultipartFile("Report_Form.xlsx","Report_Form.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",input)

            val wb:Workbook = XSSFWorkbook()
            val out = ByteArrayOutputStream()
            wb.write(out)
            out.close()
            wb.close()
            val byteArray = out.toByteArray()

            val pdfWB = XSSFWorkbook()

            every { reportService.importReportFormat( multipartFile ) } returns wb
            every { studentService.getAllByGrade( grade ) } returns student
            every { studentService.getAllByGrade( any() ) } returns student
            every { studentService.convertListStudentToString(student) } returns studentString
            every { reportService.exportReportWorkbook(wb,studentString,any(),any(),any()) } returns pdfWB
            every { reportService.convertWorkBookToPDF(pdfWB) } returns byteArray

            studentService.exportStudentByGradePDF(multipartFile,grade)

            verify(exactly = 1) { reportService.importReportFormat(multipartFile) }
            verify(exactly = 1) { reportService.importReportFormat(any()) }

            verify(exactly = 1) { studentService.getAllByGrade(grade) }
            verify(exactly = 1) { studentService.getAllByGrade(any()) }

            verify(exactly = 1) { studentService.convertListStudentToString(student) }
            verify(exactly = 1) { studentService.convertListStudentToString(any()) }

            verify(exactly = 1) { reportService.exportReportWorkbook(wb,studentString,any(),any(),any()) }

            verify(exactly = 1) { reportService.convertWorkBookToPDF(pdfWB) }
            verify(exactly = 1) { reportService.convertWorkBookToPDF(any()) }
        }

        @Test
        fun `should throw GradeException`() {
            val wb:Workbook = XSSFWorkbook()
            val grade = 16.9
            var file = File("./form/Report_Form.xlsx")
            var input = FileInputStream(file)
            var multipartFile = MockMultipartFile("Report_Form.xlsx","Report_Form.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",input)

            val pdfWB = XSSFWorkbook()
            var studentString: MutableList<MutableList<String>> = mutableListOf(
                mutableListOf("Lorem","Ipsum","75","3.5","19","2002-07-21"),
                mutableListOf("Peacefully","Rifle","77","3.5","19","2002-05-23")
            )

            var student:List<Student> = listOf(
                Student(
                    firstName = "Lorem",
                    lastName = "Ipsum",
                    score = 75,
                    grade = grade,
                    age = 19,
                    birthDate = LocalDate.parse("2002-07-21")
                ),
                Student(
                    firstName = "Peacefully",
                    lastName = "Rifle",
                    score = 77,
                    grade = grade,
                    age = 19,
                    birthDate = LocalDate.parse("2002-05-23")
                )
            )

            assertThrows<Exception> { studentService.exportStudentByGradePDF(multipartFile,grade) }

            verify(exactly = 0) { reportService.importReportFormat(multipartFile) }
            verify(exactly = 0) { reportService.importReportFormat(any()) }

            verify(exactly = 0) { studentService.getAllByGrade(grade) }
            verify(exactly = 0) { studentService.getAllByGrade(any()) }

            verify(exactly = 0) { studentService.convertListStudentToString(student) }
            verify(exactly = 0) { studentService.convertListStudentToString(any()) }

            verify(exactly = 0) { reportService.exportReportWorkbook(wb,studentString,any(),any(),any()) }

            verify(exactly = 0) { reportService.convertWorkBookToPDF(pdfWB) }
            verify(exactly = 0) { reportService.convertWorkBookToPDF(any()) }
        }
    }

    @Nested
    @DisplayName("importStudent()")
    inner class ImportStudent() {
        @Test
        fun `should import student`() {
            var studentString: MutableList<MutableList<String>> = mutableListOf(
                mutableListOf("19","Lorem","Ipsum","75","3.5","21-07-2002"),
                mutableListOf("19","Peacefully","Rifle","77","3.5","23-05-2002")
            )

            var studentDTO1:StudentDTO = StudentDTO(
                firstName = "Lorem",
                lastName = "Ipsum",
                score = 75,
                grade = 3.5,
                age = 19,
                birthDate = LocalDate.parse("2002-07-21")
            )

            var studentDTO2:StudentDTO = StudentDTO(
                firstName = "Peacefully",
                lastName = "Rifle",
                score = 77,
                grade = 3.5,
                age = 19,
                birthDate = LocalDate.parse("2002-05-23")
            )

            every { studentService.createStudent( studentDTO1 ) } returns studentDTO1
            every { studentService.createStudent( studentDTO2 ) } returns studentDTO2

            studentService.importStudent(studentString)

            verify(exactly = 1) { studentService.createStudent(studentDTO1) }
            verify(exactly = 1) { studentService.createStudent(studentDTO1) }
            verify(exactly = 2) { studentService.createStudent(any()) }

        }
    }

}