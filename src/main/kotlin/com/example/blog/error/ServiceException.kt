package com.example.blog.error

import com.example.blog.enum.ErrorStatus

open class ServiceException (val errorStatus: ErrorStatus ) : Exception()