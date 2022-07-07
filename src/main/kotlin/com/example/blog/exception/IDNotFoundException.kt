package com.example.blog.exception

import com.example.blog.enum.ErrorStatus
import com.example.blog.error.ServiceException


class IDNotFoundException : ServiceException(ErrorStatus.ID_NOT_FOUND)