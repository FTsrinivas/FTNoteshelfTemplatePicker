package com.fluidtouch.noteshelf.generator

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.Size
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateGenerator
import com.fluidtouch.dynamicgeneration.generators.FTOtherTemplatesDynamicGenerator
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentType
import com.fluidtouch.noteshelf.documentframework.FTUrl
import com.fluidtouch.noteshelf.generator.formats.dayandnight_journal.FTDiaryGeneratorV2
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo
import com.fluidtouch.noteshelf.models.theme.FTNAutoTemlpateDiaryTheme
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme
import com.fluidtouch.noteshelf.models.theme.FTNTheme
import java.util.*

interface FTAutoTemplateGenerationCallback {
    fun onGenerated(documentInfo: FTDocumentInputInfo?, generationError: Error?)
}

abstract class FTAutoTemplateGenerator {
    abstract fun generate(context: Context, callback: FTAutoTemplateGenerationCallback)

    /*fun generateTemplate (context: Context, callback: FTAutoTemplateGenerationCallback) {
        val obj = PDFGenAsyncTask();
        obj.executeOnExecutor();
    }*/

    companion object {
        @JvmStatic
        fun autoTemplateGenerator(theme: FTNTheme): FTAutoTemplateGenerator {

            if (theme.dynamicId == 1) {
                if (theme.template_id.equals("DayAndNight")) {
                    return FTAutoTemplateDayAndNightJournalGenerator(theme)
                } else {
                    return FTAutoTemplateDiaryGenerator(theme)
                }
            } else if (theme.dynamicId == 2) {
                return FTAutoDynamicTemplateGenerator(theme)
            } else if (theme.dynamicId == 3) {
                return FTAutoOtherTemplatesDynamicGenerator(theme)
            }
            return FStandardTemplateDiaryGenerator(theme)
        }
    }

    internal class FStandardTemplateDiaryGenerator(inTheme: FTNTheme) : FTAutoTemplateGenerator() {
        val theme = inTheme

        override fun generate(context: Context, callback: FTAutoTemplateGenerationCallback) {
            val info = FTDocumentInputInfo()
            info.inputFileURL = FTUrl.parse(theme.themeTemplateURL().getPath())
            info.isTemplate = theme.isTemplate()
            if (theme.isDownloadTheme || theme.isCustomTheme) {
                info.isTemplate = true
            }
            info.footerOption = theme.themeFooterOption
            info.isNewBook = true
            info.lineHeight = theme.lineHeight
            //TODO:
            //Call back should return URL and error
            // The pdf should be genrated at some temporary location
            callback.onGenerated(documentInfo = info, generationError = null)
        }
    }

    internal class FTAutoDynamicTemplateGenerator(inTheme: FTNTheme) : FTAutoTemplateGenerator() {
        val theme = inTheme as FTNDynamicTemplateTheme
        override fun generate(context: Context, callback: FTAutoTemplateGenerationCallback) {

            val generator = FTDynamicTemplateGenerator(
                theme.templateInfoDict,
                theme.isLandscape, getSize(), context
            )
            val info = FTDocumentInputInfo()
            info.inputFileURL = generator.generate(theme);
            info.isTemplate = theme.isTemplate()
            if (theme.isDownloadTheme || theme.isCustomTheme) {
                info.isTemplate = true
            }
            info.footerOption = theme.themeFooterOption
            info.isNewBook = true
            info.lineHeight = theme.lineHeight

            callback.onGenerated(documentInfo = info, generationError = null)
        }

        companion object {
            private fun getSize(): Size {
                val displayMetrics = Resources.getSystem().displayMetrics
                return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
            }
        }
    }

    internal class FTAutoTemplateDiaryGenerator(inTheme: FTNTheme) : FTAutoTemplateGenerator() {
        val theme = inTheme as FTNAutoTemlpateDiaryTheme

        override fun generate(context: Context, callback: FTAutoTemplateGenerationCallback) {
            if (theme.startDate == null) {
                theme.startDate = FTApp.getPref().diaryRecentStartDate
            }
            if (theme.endDate == null) {
                val calendar = GregorianCalendar(context.resources.configuration.locales[0])
                calendar.time = FTApp.getPref().diaryRecentEndDate
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                theme.endDate = calendar.time
            }

            val yearFormatInfo =
                FTYearFormatInfo(
                    theme.startDate,
                    theme.endDate,
                    theme.template_id,
                    theme.isLandscape
                )
            val generator = FTDiaryGenerator(
                theme,
                context,
                FTDiaryFormat.getFormat(context, yearFormatInfo),
                yearFormatInfo
            )
            val info = FTDocumentInputInfo()
            info.inputFileURL = generator.generate();
            info.isTemplate = theme.isTemplate()
            if (theme.isDownloadTheme || theme.isCustomTheme) {
                info.isTemplate = true
            }
            info.footerOption = theme.themeFooterOption
            info.isNewBook = true
            info.lineHeight = theme.lineHeight

            val postProcessInfo = FTDocumentInputInfo.FTPostProcessInfo()
            postProcessInfo.startDate = theme.startDate
            postProcessInfo.endDate = theme.endDate
            postProcessInfo.offsetCount = generator.offsetCount + 1
            postProcessInfo.documentType = FTDocumentType.autoGeneratedDiary

            info.postProcessInfo = postProcessInfo

            callback.onGenerated(documentInfo = info, generationError = null)
        }
    }

    internal class FTAutoTemplateDayAndNightJournalGenerator(inTheme: FTNTheme) :
        FTAutoTemplateGenerator() {
        val theme = inTheme as FTNAutoTemlpateDiaryTheme

        override fun generate(context: Context, callback: FTAutoTemplateGenerationCallback) {
            if (theme.startDate == null) {
                theme.startDate = FTApp.getPref().diaryRecentStartDate
            }
            if (theme.endDate == null) {
                val calendar = GregorianCalendar(context.resources.configuration.locales[0])
                calendar.time = FTApp.getPref().diaryRecentEndDate
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                theme.endDate = calendar.time
            }

            val yearFormatInfo =
                FTYearFormatInfo(
                    theme.startDate,
                    theme.endDate,
                    theme.template_id,
                    theme.isLandscape
                )

            yearFormatInfo.isTablet = theme.isTablet;

            val generator = FTDiaryGeneratorV2(
                theme,
                context,
                FTDiaryFormat.getFormat(context, yearFormatInfo),
                yearFormatInfo
            )
            val info = FTDocumentInputInfo()
            info.inputFileURL = generator.generate();
            info.isTemplate = theme.isTemplate()
            if (theme.isDownloadTheme || theme.isCustomTheme) {
                info.isTemplate = true
            }
            info.footerOption = theme.themeFooterOption
            info.isNewBook = true
            info.lineHeight = theme.lineHeight

            val postProcessInfo = FTDocumentInputInfo.FTPostProcessInfo()
            postProcessInfo.startDate = theme.startDate
            postProcessInfo.endDate = theme.endDate
            postProcessInfo.offsetCount = 0
            postProcessInfo.documentType = FTDocumentType.autoGeneratedDiary

            info.postProcessInfo = postProcessInfo

            callback.onGenerated(documentInfo = info, generationError = null)
        }
    }

    internal class FTAutoOtherTemplatesDynamicGenerator(inTheme: FTNTheme) :
        FTAutoTemplateGenerator() {
        val theme = inTheme as FTNDynamicTemplateTheme
        override fun generate(context: Context, callback: FTAutoTemplateGenerationCallback) {
            Log.d(
                "TemplateFormat==>",
                "FTAutoOtherTemplatesDynamicGenerator theme.isLandscape::-" + theme.isLandscape + " themeName::-" + theme.themeName
            );
            val generator = FTOtherTemplatesDynamicGenerator(context)
            val info = FTDocumentInputInfo()
            info.inputFileURL = generator.generate(theme);
            info.isTemplate = theme.isTemplate()
            if (theme.isDownloadTheme || theme.isCustomTheme) {
                info.isTemplate = true
            }
            info.footerOption = theme.themeFooterOption
            info.isNewBook = true
            info.lineHeight = theme.lineHeight

            callback.onGenerated(documentInfo = info, generationError = null)
        }

        companion object {
            private fun getSize(): Size {
                val displayMetrics = Resources.getSystem().displayMetrics
                return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
            }
        }
    }
}