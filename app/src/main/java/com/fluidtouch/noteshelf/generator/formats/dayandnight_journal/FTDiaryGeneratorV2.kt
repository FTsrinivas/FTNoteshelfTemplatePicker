package com.fluidtouch.noteshelf.generator.formats.dayandnight_journal

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.example.demo.generator.models.info.rects.FTDairyDayPageRect
import com.example.demo.generator.models.info.rects.FTDairyYearPageRect
import com.fluidtouch.noteshelf.models.theme.FTNAutoTemlpateDiaryTheme
import com.fluidtouch.noteshelf.generator.FTDairyRenderFormat
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo
import com.fluidtouch.noteshelf.generator.models.info.FTYearInfoMonthly
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil
import com.fluidtouch.noteshelf.documentframework.FTUrl
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil
import com.fluidtouch.noteshelf.preferences.SystemPref
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

class FTDiaryGeneratorV2(
    private val theme: FTNAutoTemlpateDiaryTheme,
    private val context: Context,
    private val format: FTDairyRenderFormat,
    private val formatInfo: FTYearFormatInfo) {

    private val monthlyFormatter: FTYearInfoMonthly
    var offsetCount = 0
    private var ftSelectedDeviceInfo: FTSelectedDeviceInfo
    var cachePath = FTConstants.TEMP_FOLDER_PATH + "TemplatesCache/"

    init {
        monthlyFormatter = FTYearInfoMonthly(formatInfo)
        ftSelectedDeviceInfo = FTSelectedDeviceInfo()
    }

    fun generate(): FTUrl {
        val startDate: Calendar = GregorianCalendar(formatInfo.locale)
        startDate.time = formatInfo.startMonth
        startDate[startDate[Calendar.YEAR], startDate[Calendar.MONTH]] =
            startDate.getActualMinimum(Calendar.DAY_OF_MONTH)
        val endDate: Calendar = GregorianCalendar(formatInfo.locale)
        endDate.time = formatInfo.endMonth
        endDate[endDate[Calendar.YEAR], endDate[Calendar.MONTH]] =
            endDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        monthlyFormatter.generate()
        FTYearFormatInfo.calendarYearHeading =   FTYearFormatInfo.getYearTitle( startDate[Calendar.YEAR].toString(),endDate[Calendar.YEAR].toString())

        // Core logic for generating the pdf with all formats (year, month...)
        val document = PdfDocument()
        format.setDocument(document)

        format.renderYearPage(context, monthlyFormatter.monthInfos, formatInfo)

        val fileName = startDate[Calendar.YEAR].toString() + "_" + endDate[Calendar.YEAR]
        FTApp.getPref().save(SystemPref.DIARY_CREATION_YEAR, fileName)
        val url = writeToTheDocument(document, "$fileName.pdf")
        document.close()
        createPageHyperLinks(url)
        val filePath = FTConstants.TEMP_FOLDER_PATH + fileName
        pdfToBitmap(theme, File(url), fileName, filePath)
        return FTUrl.parse(url)
    }

    private fun writeToTheDocument(document: PdfDocument, filename: String): String {
        val rootPath = FTConstants.TEMP_FOLDER_PATH
        val rootFile = File(rootPath)
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }
        val filePath = rootPath + filename
        try {
            document.writeTo(FileOutputStream(filePath))
        } catch (e: IOException) {
            Log.e(FTDiaryGeneratorV2::class.java.name, e.toString())
        }
        return filePath
    }


    protected fun pdfToBitmap(theme: FTNAutoTemlpateDiaryTheme, pdfFile: File?, fileName: String, filePath: String) {
        /*
         * Conversion of PDF to Image format
         * */
        val bitmaps = ArrayList<Bitmap>()
        try {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            val bitmap: Bitmap
            val page = renderer.openPage(0)
            val width = page.width
            val height = page.height
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            // close the page
            page.close()
            // close the renderer
            renderer.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!bitmaps.isEmpty()) {
            storeImage(theme, bitmaps[0], fileName, filePath)
        }
    }

    private fun storeImage(theme: FTNAutoTemlpateDiaryTheme, image: Bitmap, fileName: String, filePath: String) {

        var thmumbNailName: String?
        ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo()
        val tabSelected = ftSelectedDeviceInfo.layoutType
        Log.d("TemplatePicker==>", "FTDiaryGenerator storeImage getPageWidth::-" + ftSelectedDeviceInfo.pageWidth
                    + " getPageHeight::-" + ftSelectedDeviceInfo.pageHeight + " tabSelected::-" + tabSelected + " theme.categoryName::-" + theme.categoryName
        )
        var thumbnailWidth = ftSelectedDeviceInfo.pageWidth
        var thumbnailHeight = ftSelectedDeviceInfo.pageHeight
        if (thumbnailWidth == 0 && thumbnailHeight == 0) {
            thumbnailWidth = ScreenUtil.getScreenWidth(FTApp.getInstance().applicationContext)
            thumbnailHeight = ScreenUtil.getScreenHeight(FTApp.getInstance().applicationContext)
        }
        thmumbNailName = if (tabSelected.equals("portrait", ignoreCase = true)) {
            "thumbnail_" + fileName + "_" + thumbnailWidth + "_" + thumbnailHeight + "_port_.jpg"
        } else {
            "thumbnail_" + fileName + "_" + thumbnailWidth + "_" + thumbnailHeight + "_land_.jpg"
        }

        saveInCache(thmumbNailName, image)
        /*
         * Saving bitmap to internal storage
         * */
        val pictureFile = File(filePath + thmumbNailName)
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveInCache(thmumbNailName: String, image: Bitmap) {
        /*
         * Saving bitmap to internal storage
         * */
        val tempCacheFiles: File = File(cachePath)
        if (!tempCacheFiles.exists()) {
            tempCacheFiles.mkdir()
        }
        val pictureFile: File = File(cachePath + thmumbNailName)
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createPageHyperLinks(url : String) {
        try {
            val pdfFile = File(url)
            val pdDocument: PDDocument = PDDocument.load(pdfFile)
            var pageIndex = 1
            var count = -1
            var yearPage = pdDocument.getPage(pageIndex)
            FTDairyYearPageRect.yearRectInfo.forEach{ arrayList ->
                arrayList.forEach { monthRect ->
                    pageIndex++
                    count++
                    val page = pdDocument.getPage(pageIndex)
                    if (page != null) {
                        linkDayPageToYear(count, page, yearPage)
                        val link = PDAnnotationLink()
                        val destination: PDPageDestination = PDPageFitWidthDestination()
                        val action = PDActionGoTo()

                        destination.page = page
                        action.destination = destination
                        link.action = action

                        val pdRectangle = PDRectangle()
                        pdRectangle.lowerLeftX = monthRect.left
                        pdRectangle.lowerLeftY = page.mediaBox.height - monthRect.top
                        pdRectangle.upperRightX = monthRect.right
                        pdRectangle.upperRightY = page.mediaBox.height - monthRect.bottom
                        link.rectangle = pdRectangle
                        yearPage.getAnnotations().add(link)
                    }
                }
            }
            pdDocument.save(pdfFile)
            pdDocument.close()
         } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun linkDayPageToYear(index: Int, page: PDPage, yearPage: PDPage) {
        val link = PDAnnotationLink()
        val destination: PDPageDestination = PDPageFitWidthDestination()
        val action = PDActionGoTo()

        destination.page = yearPage
        action.destination = destination
        link.action = action

        val pdRectangle = PDRectangle()
        pdRectangle.lowerLeftX = FTDairyDayPageRect.yearPageRect.get(index).left.toFloat()
        pdRectangle.lowerLeftY =
            page.mediaBox.height - FTDairyDayPageRect.yearPageRect.get(index).top
        pdRectangle.upperRightX = FTDairyDayPageRect.yearPageRect.get(index).right.toFloat()
        pdRectangle.upperRightY =
            page.mediaBox.height - FTDairyDayPageRect.yearPageRect.get(index).bottom

        link.rectangle = pdRectangle
        page.getAnnotations().add(link)

    }



}