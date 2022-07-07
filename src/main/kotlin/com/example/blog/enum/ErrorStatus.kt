package com.example.blog.enum

import org.springframework.http.HttpStatus

enum class ErrorStatus(val httpStatus: HttpStatus, val message:String) {
    ID_NOT_FOUND(HttpStatus.NOT_FOUND,"This ID does not exist")
}