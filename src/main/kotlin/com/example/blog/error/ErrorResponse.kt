package com.example.blog.error

import com.example.blog.enum.ErrorStatus

data class ErrorResponse(
    val responseCode: Int,
    val responseStatus: String,
    val errors: List<ApiError>
) {
    class ApiError(
        val errorCode: String,
        val errorMessage: String
    )

    companion object {
        fun fromErrorCode(errorCode: ErrorStatus) = ErrorResponse(
            errorCode.httpStatus.value(),
            errorCode.httpStatus.name,
            arrayListOf(
                ApiError(
                    errorCode.name,
                    errorCode.message
                )
            )
        )
    }
}