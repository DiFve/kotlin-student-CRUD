package com.example.blog.service

import com.example.blog.enum.CustomCellStyle
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


@Service
class ReportServiceInterfaceImpl(
    private val stylesExcelService: StylesExcelService,

    ) : ReportServiceInterface {
    var logger: Logger = LoggerFactory.getLogger(this::class.java)


    override fun  exportReportWorkbook(wb:Workbook,dataList: MutableList<MutableList<String>>, columnName: List<String>, sheetName:String, startRow: Int):Workbook {
        val styles = stylesExcelService.prepareStyles(wb)
        lateinit var sheet: Sheet
        if(wb.numberOfSheets == 0){
            sheet = wb.createSheet(sheetName)
        }
        else{
            sheet = wb.getSheetAt(0)
        }
        setColumnsWidth(sheet)

        var allReport: Int = 0
        createHeader(sheet, styles,columnName,startRow)
        dataList.mapIndexed { index, value ->

            if(allReport+2 >= 17){
                logger.info("Report: ${allReport+2}")
                sheet.shiftRows(allReport+2,allReport+2,1)
            }
            createData(sheet,styles,value,index+1+startRow)
            allReport++
        }
        if(allReport+2 >= 17){
            insertData(sheet,styles[CustomCellStyle.ORANGE], "$allReport",allReport+2,1)
        }
        else{
            insertData(sheet,styles[CustomCellStyle.ORANGE], "$allReport",17,1)
        }
        logger.info("Write $allReport report in $sheetName sheet")
        return wb

    }

    override fun convertWorkbookToByte(wb: Workbook): ByteArray {
        val out = ByteArrayOutputStream()
        wb.write(out)

        out.close()
        wb.close()

        return out.toByteArray()
    }

    private fun setColumnsWidth(sheet: Sheet) {
        for (columnIndex in 0 until 10) {
            sheet.setColumnWidth(columnIndex, 256 * 15)
        }
    }

    private fun createHeader(sheet: Sheet, styles: Map<CustomCellStyle, CellStyle>, columnName: List<String>,startRow: Int) {
       //val row = sheet.createRow(startRow)
        val row = sheet.getRow(1)
        columnName.mapIndexed { index, s ->

            createCell(sheet,styles[CustomCellStyle.GREY_CENTERED_BOLD_ARIAL_WITH_BORDER],row,index,s)
        }
    }


    private fun createData(
        sheet: Sheet, styles: Map<CustomCellStyle, CellStyle>,
        value: List<String>,
        row: Int
    ) {

        val row = sheet.createRow(row)

        var columnCount = 0
        value.map{
            if(columnCount == 3){
                columnCount = 14
                createCell(sheet,styles[CustomCellStyle.GREY],row,columnCount++,it)
                return@map
            }
            createCell(sheet,styles[CustomCellStyle.RIGHT_ALIGNED],row,columnCount++,it)
        }
    }

    private fun createData(
        sheet: Sheet,
        style: CellStyle?,
        value: String,
        row: Int,
        column: Int
    ) {
        val row = sheet.createRow(row)
        createCell(sheet,style,row,column,value)
    }

    private fun insertData(
        sheet: Sheet, style: CellStyle?,
        value: String,
        row: Int,
        colunmn: Int
    ) {
        val row = sheet.getRow(row)
        createCell(sheet,style,row,colunmn,value)
    }

    private fun createCell(sheet: Sheet, style: CellStyle?,row: Row, column: Int,value: String) {
        val cell = row.createCell(column)
        cell.setCellValue(value)
        cell.cellStyle = style
    }


    override fun createResponseEntity(
        report: ByteArray,
        fileName: String
    ): ResponseEntity<ByteArray> {
        logger.info("Generate $fileName")
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .body(report)
    }

    override fun importReport(file: MultipartFile):MutableList<MutableList<String>> {
        if (this.hasExcelFormat(file)) {
            logger.info("Import ${file.originalFilename}")
            return this.excelToList(file)
        } else {
            logger.warn("Not excel format")
            throw RuntimeException("Not excel format")
        }
    }

    override fun importReportFormat(file: MultipartFile): Workbook {
        if (this.hasExcelFormat(file)) {
            logger.info("Import ${file.originalFilename}")
            return XSSFWorkbook(file.inputStream)
        } else {
            logger.warn("Not excel format")
            throw RuntimeException("Not excel format")
        }
    }

    override fun convertWorkBookToPDF(wb:Workbook): ByteArray {
        val sheet:Sheet = wb.getSheetAt(0)
        val headerList = setHeader(sheet)
        val out = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, out)
        document.open()
        val table = PdfPTable(sheet.getRow(0).physicalNumberOfCells)
        //addPDFData(true, headerList, table)
        for (i in 0 until sheet.physicalNumberOfRows) {
            lateinit var rowList: MutableList<String>
            if(i==sheet.physicalNumberOfRows-1 && i<=17){
                rowList = getRowData(17, sheet).toMutableList()
            }
            else{
                rowList = getRowData(i, sheet).toMutableList()
            }


            if(i==0){
                var p:Paragraph = Paragraph(rowList[0])
                p.alignment = Element.ALIGN_CENTER
                document.add(p)
                var p1:Paragraph = Paragraph(" ")
                p1.alignment = Element.ALIGN_CENTER
                document.add(p1)
                continue
            }
            if(rowList.size == 4){
                for(i in 3 until 14){
                    rowList.add(i," ")
                }
            }

            if(rowList.size == 2){
                for(i in 0 until 13){
                    rowList.add("")
                }
            }
//            logger.info("${rowList.toString()}")

            if(i==sheet.physicalNumberOfRows-1){
                addPDFData("Total", rowList, table)
            }
            else{
                addPDFData("", rowList, table)
            }
        }
        val columnWidths = floatArrayOf(15f, 15f,15f, 2f,2f, 2f,2f, 2f,2f, 2f,2f, 2f,2f, 2f,15f)
        table.setWidths(columnWidths)
        document.add(table)
        document.close()
        logger.info("Write ${sheet.physicalNumberOfRows-2} report in pdf")
        return out.toByteArray()
    }


    private fun getRowData(index: Int, sheet: Sheet): List<String> {
        return getRow(index, sheet)
    }


    private fun addPDFData(type: String, list: List<String>, table: PdfPTable) {

        list.forEachIndexed { index,column: String? ->
                val cell = PdfPCell()
                if(index == 14){
                    cell.backgroundColor = BaseColor(191,191,191)
                }
                if (type=="Total"){
                    cell.backgroundColor = BaseColor(255,153,0)
                }
                cell.phrase = Phrase(column)
                table.addCell(cell)
        }

    }

    private fun setHeader(sheet: Sheet):List<String>{
        return getRow(0,sheet)
    }

    fun getRow(index: Int, sheet: Sheet): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        for (cell:Cell in sheet.getRow(index)) {
            //logger.info("Index: $index value:${cell.cellType}")
            list.add(cell.stringCellValue)
        }
        return list
    }

    private fun hasExcelFormat(file: MultipartFile): Boolean = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" == file.contentType

    private fun excelToList(file: MultipartFile):MutableList<MutableList<String>> {
        try {
            val wb: Workbook = XSSFWorkbook(file.inputStream)
            val sheet: Sheet = wb.getSheetAt(0)
            val rows: Iterator<Row> = sheet.iterator()
            var rowNumber = 0

            var allValueList : MutableList<MutableList<String>> = mutableListOf()
            while (rows.hasNext()) {
                val currentRow = rows.next()
                // skip header
                if (rowNumber == 0) {
                    rowNumber++
                    continue
                }

                var rowValueList : MutableList<String> = mutableListOf()
                currentRow.forEach{
                    rowValueList.add(it.stringCellValue)
                }
                allValueList.add(rowValueList)
                rowNumber++
            }
            wb.close()
            logger.info("Convert ${file.originalFilename} to ${rowNumber-1} report")
            return allValueList
        } catch (e: Exception) {
            logger.info(e.stackTraceToString())
            throw RuntimeException("Internal server error")
        }
    }
}