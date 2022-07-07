package com.example.blog.service

import org.apache.poi.ss.usermodel.Workbook
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

interface ReportServiceInterface {
    fun exportReportWorkbook(wb:Workbook,dataList: MutableList<MutableList<String>>, columnName: List<String>, sheetName:String,startRow:Int): Workbook
    fun convertWorkbookToByte(wb:Workbook): ByteArray
    fun createResponseEntity(report: ByteArray, fileName: String): ResponseEntity<ByteArray>
    fun importReport(file: MultipartFile):MutableList<MutableList<String>>
    fun importReportFormat(file: MultipartFile):Workbook
    fun convertWorkBookToPDF(wb: Workbook): ByteArray
}