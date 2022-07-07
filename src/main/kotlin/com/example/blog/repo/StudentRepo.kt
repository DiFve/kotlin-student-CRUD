package com.example.blog.repo

import com.example.blog.model.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentRepo: JpaRepository<Student, Long> {
    fun findByFirstName(firstName: String): Iterable<Student>
    fun findAllByGrade(grade: Double): List<Student>
}
