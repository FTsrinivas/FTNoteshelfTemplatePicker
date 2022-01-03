package com.fluidtouch.noteshelf.generator.formats.dayandnight_journal

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.SizeF
import androidx.core.content.res.ResourcesCompat
import com.example.demo.generator.models.info.rects.FTDairyDayPageRect
import com.example.demo.generator.models.info.rects.FTDairyYearPageRect
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.commons.utils.FTScreenUtils
import com.fluidtouch.noteshelf.generator.FTDiaryFormat
import com.fluidtouch.noteshelf.generator.models.info.FTMonthInfo
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo
import com.fluidtouch.noteshelf2.R
import java.util.*
import kotlin.collections.ArrayList


const val calendarYearTextSpace = 16

class FTDayAndNightJournal(
    private val context: Context,
    private val info: FTYearFormatInfo,
) : FTDiaryFormat(context, info) {

    private var quotesList = ArrayList<QuoteItem>()

    private var scaleFactor = 0f

    private var heightPercent = 0f
    private var widthPercent = 0f

    private var pageTopPadding = 0f
    private var pageLeftPadding = 0f
    private var pageBottomPadding = 0f

    //Intropage

    private var dy: Float = 0f
    private var introPageLeftMargin: Float = 0f

    //Calendar Page measurements
    private var maxRows = 0
    private var maxColumns = 0
    private var maxLines = 0
    private var calendarTopManualSpace = 0f
    private var calendarVerticalSpacing = 0f
    private var calendarHorizontalSpacing = 0f
    private var calendarDayTextSize = 0f
    private var calendarMonthTextSize = 0f
    private var calendarYearTextSize = 0f
    private var dayLeftMargin = 0f
    private var dayTopMargin = 0f
    private var calendarBoxWidth = 0f
    private var calendarBoxHeight = 0f
    private var individualDayinCalendar = 0f
    private var templateDottedLineGap = 0f

    //Day Page measurements
    private var dairyTextSize = 0f
    private var mTop = 0f
    private var mBottom = 0f
    private lateinit var canvas: Canvas

    private var quoteTextHeight = 0
    private var dateHeading = ""

    init {
        heightPercent = info.screenSize.height / 100f
        widthPercent = info.screenSize.width / 100f
    }

    override fun renderYearPage(
        context: Context,
        months: List<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        super.renderYearPage(context, months, calendarYear)
        dateHeading = FTYearFormatInfo.calendarYearHeading
        lateinit var thSize: SizeF
        thSize = SizeF(info.screenSize.width.toFloat(), info.screenSize.height.toFloat())

        var aspectSize: SizeF = FTScreenUtils.aspectSize(thSize, SizeF(800f, 1200f))
        if (info.isLandscape) {
            aspectSize = FTScreenUtils.aspectSize(thSize, SizeF(1200f, 800f))
        }
        scaleFactor = thSize.getWidth() / aspectSize.width

        calenderDynamicSizes
        quotesList = FTScreenUtils.getQuotesList(context)
        createIntroPage()
        createCalendarPage(months, calendarYear)
        createTemplate(months)

    }

    private val calenderDynamicSizes: Unit
        get() {
            //Portrait
            introPageLeftMargin = widthPercent * 7.125f
            pageTopPadding = heightPercent * 3.5f
            pageLeftPadding = widthPercent * 5.155f
            pageBottomPadding = heightPercent * 3.58f
            calendarVerticalSpacing = heightPercent * 1.81f
            calendarHorizontalSpacing = widthPercent * 1.81f
            calendarTopManualSpace = heightPercent * 10.58f
            maxRows = FTScreenUtils.findRowCount(info.isLandscape)
            maxColumns = FTScreenUtils.findColumnCount(info.isLandscape)
            maxLines = FTScreenUtils.findMaxLines(info.isLandscape)
            templateDottedLineGap = heightPercent * 2.91f
            if (info.isLandscape) {
                introPageLeftMargin = widthPercent * 8.1f
                pageLeftPadding = widthPercent * 3.125f
                pageTopPadding = heightPercent * 5.5f
                calendarVerticalSpacing = heightPercent * 2.79f
                calendarHorizontalSpacing = widthPercent * 1.95f
                calendarTopManualSpace = heightPercent * 15.27f
                templateDottedLineGap = heightPercent * 4.44f
            }
            findBoxWidth(maxColumns)
            findBoxHeight(maxRows)

            calendarDayTextSize = 10 * scaleFactor
            calendarMonthTextSize = 15 * scaleFactor
            calendarYearTextSize = 40 * scaleFactor
            dayLeftMargin = widthPercent * 3f
            dayTopMargin = heightPercent * 1.94f
            individualDayinCalendar = calendarBoxWidth / 7
            dairyTextSize = heightPercent * 1.66f
        }

    private fun createIntroPage() {
        val startingPage = getPage(1)
        canvas = startingPage.canvas

        canvas.drawRect(
            RectF(0f, 0f, info.screenSize.width.toFloat(), info.screenSize.height.toFloat()),
            FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.introQuote_Paint.textSize = 25 * scaleFactor

        val introQuoteBoxHeight = if (!info.isLandscape) {
            heightPercent * 29.91f
        } else {
            heightPercent * 32.22f
        }
        canvas.drawRect(
            RectF(0f, 0f, info.screenSize.width.toFloat(), introQuoteBoxHeight),
            FTDairyTextPaints.coloredBoxPaint
        )
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_quote),
            (info.screenSize.width / 2).toFloat(),
            introQuoteBoxHeight / 2,
            FTDairyTextPaints.introQuote_Paint
        )
        FTDairyTextPaints.introAuthorText_Paint.textSize = 22 * scaleFactor
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_author_name),
            (info.screenSize.width / 2).toFloat(),
            (24 * scaleFactor + 30 + introQuoteBoxHeight / 2),
            FTDairyTextPaints.introAuthorText_Paint
        )
        var introPageHeight = introQuoteBoxHeight + 14.0f * heightPercent
        if (info.isLandscape) {
            introPageHeight =  introQuoteBoxHeight + 9.91f * heightPercent
        }

        FTDairyTextPaints.introText_Paint.textSize = 35 * scaleFactor
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_title),
            (info.screenSize.width / 2).toFloat(),
            introPageHeight,
            FTDairyTextPaints.introText_Paint
        )
        introPageHeight += introPageHeight + 5f * heightPercent
        var pointLeftmargins = 8.125f * widthPercent
        dy = 48 * heightPercent
        val textPaint = TextPaint().apply {
            letterSpacing = 0.025f
            textSize = 25 * scaleFactor
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_regular)
            color = context.resources.getColor(R.color.text_color, context.theme)
        }
        val rect = Rect()
        textPaint.getTextBounds("•-", 0, "•-".length, rect)
        drawBullet(dy)

        pointLeftmargins += rect.width()
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_point_one),
            pointLeftmargins,
            dy,
            isBulletPoint = true
        )
        drawBullet(dy)
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_point_two),
            pointLeftmargins,
            dy,
            isBulletPoint = true
        )
        drawBullet(dy)
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_point_three),
            pointLeftmargins,
            dy,
            isBulletPoint = true
        )
        dy += 4.1f * heightPercent
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_static_para),
            introPageLeftMargin,
            dy, false
        )
        document.finishPage(startingPage)
    }

    private fun createCalendarPage(months: List<FTMonthInfo>, calendarYear: FTYearFormatInfo) {
        val thirdPage = getPage(2)
        canvas = thirdPage.canvas
        canvas.drawRect(
            RectF(
                0f, 0f, info.screenSize.width.toFloat(), info.screenSize.height
                    .toFloat()
            ), FTDairyTextPaints.background_Paint
        )

        FTDairyTextPaints.calendar_Year_Paint.textSize = calendarYearTextSize
        FTDairyTextPaints.calendar_Month_Paint.textSize = calendarMonthTextSize
        FTDairyTextPaints.calendar_WeekDays_Paint.textSize = calendarDayTextSize
        FTDairyTextPaints.calendar_Days_Paint.textSize = calendarDayTextSize

        val rect = Rect()
        val yearText = dateHeading
        FTDairyTextPaints.calendar_Year_Paint.getTextBounds(yearText, 0, yearText.length, rect)

        canvas.drawText(
            dateHeading,
            pageLeftPadding,
            pageTopPadding + calendarYearTextSize,
            FTDairyTextPaints.calendar_Year_Paint
        )

        var boxLeft: Float
        var boxTop = calendarTopManualSpace
        var boxRight: Float
        var boxBottom = boxTop + calendarBoxHeight
        var month_Of_Year = 0
        FTDairyYearPageRect.yearRectInfo = ArrayList()

        for (rows in 1..maxRows) {
            boxLeft = pageLeftPadding
            boxRight = pageLeftPadding + calendarBoxWidth
            if (rows > 1) {
                boxTop += calendarBoxHeight + calendarVerticalSpacing
                boxBottom = boxTop + calendarBoxHeight
            }
            for (columns in 1..maxColumns) {
                val monthRectsList = FTDairyYearPageRect().monthRectInfo
                var dayRectInfo: RectF
                var dateLeftSpace = boxLeft
                var dateTopSpace = boxTop + calendarVerticalSpacing * 3
                val r = RectF(boxLeft, boxTop, boxRight, boxBottom)
                canvas.drawRoundRect(r, 10f, 10f, FTDairyTextPaints.coloredBoxPaint)

                if (month_Of_Year <= months.size - 1) {
                    val monthName = months[month_Of_Year].monthTitle.uppercase()

                    //show Week Day Names
                    showWeekDays(months, month_Of_Year, boxLeft, dateTopSpace)
                    months[month_Of_Year].dayInfos.forEachIndexed { index, ftDayInfo ->
                        if (index % 7 == 0) {
                            dateTopSpace += dayTopMargin+3*scaleFactor
                            dateLeftSpace = boxLeft
                        }
                        val date =
                            if (ftDayInfo.belongsToSameMonth) ftDayInfo.dayString else ""
                        val xPosition = dateLeftSpace + individualDayinCalendar / 2
                        canvas.drawText(
                            date,
                            xPosition,
                            dateTopSpace,
                            FTDairyTextPaints.calendar_Days_Paint
                        )
                        val dayRectLeft = xPosition - (individualDayinCalendar / 2)

                        if (date.isNotEmpty()) {
                            dayRectInfo = RectF(
                                dayRectLeft,
                                dateTopSpace - calendarDayTextSize,
                                dayRectLeft + individualDayinCalendar - 5,
                                dateTopSpace
                            )
                            monthRectsList.add(dayRectInfo)
                        }

                        dateLeftSpace += individualDayinCalendar
                    }

                    canvas.drawText(
                        monthName,
                        boxLeft + individualDayinCalendar / 2,
                        boxTop + calendarVerticalSpacing + (heightPercent * 0.5f),
                        FTDairyTextPaints.calendar_Month_Paint
                    )
                    FTDairyYearPageRect.yearRectInfo.add(monthRectsList)
                }
                boxLeft = boxRight + calendarHorizontalSpacing
                boxRight += calendarBoxWidth + calendarHorizontalSpacing
                month_Of_Year++
            }
        }
        document.finishPage(thirdPage)
    }

    private fun createTemplate(months: List<FTMonthInfo>) {
        var pageNumber = 3
        FTDairyDayPageRect.yearPageRect = ArrayList()

        months.forEach { ftMonthInfo ->
            ftMonthInfo.dayInfos.forEach { ftDayInfo ->
                if (ftDayInfo.belongsToSameMonth) {
                    val dairyDate =
                        ftMonthInfo.monthTitle + " " + FTScreenUtils.getDayOfMonthSuffix(ftDayInfo.dayString.toInt()) + ","
                    val dairyYear = "" + ftMonthInfo.year
                    templateDayAndNight(pageNumber, dairyDate, dairyYear)
                    pageNumber += 1
                }
            }
        }

    }

    private fun showWeekDays(
        months: List<FTMonthInfo>,
        month_Of_Year: Int,
        boxLeft: Float,
        topMargin: Float
    ) {
        var dayLeftMargin = boxLeft
        for (i in 0..6) {
            val dayName = months[month_Of_Year].dayInfos[i].weekDay
            val xPosition = dayLeftMargin + individualDayinCalendar / 2
            canvas.drawText(
                dayName,
                xPosition,
                topMargin,
                FTDairyTextPaints.calendar_WeekDays_Paint
            )
            dayLeftMargin += individualDayinCalendar
        }
    }

    private fun templateDayAndNight(pageNumber: Int, date: String, year: String) {
        val page = getPage(pageNumber)
        canvas = page.canvas
        canvas.drawRect(
            RectF(
                0f, 0f, info.screenSize.width.toFloat(), info.screenSize.height
                    .toFloat()
            ), FTDairyTextPaints.background_Paint
        )

        val dayPaint = Paint()
        dayPaint.color = context.resources.getColor(R.color.text_color, context.theme)
        dayPaint.textSize = 23 * scaleFactor
        dayPaint.typeface = ResourcesCompat.getFont(context, R.font.lora_regular)

        val yearPaint = Paint()
        yearPaint.color = context.resources.getColor(R.color.day_text_color, context.theme)
        yearPaint.textSize = 23 * scaleFactor
        yearPaint.typeface = ResourcesCompat.getFont(context, R.font.lora_regular)

        var dateWithMonthRect = Rect()
        var onlyYearRect = Rect()
        var mDate = "Date : $date"
        dayPaint.getTextBounds(mDate, 0, mDate.length, dateWithMonthRect)

        yearPaint.getTextBounds(year, 0, year.length, onlyYearRect)

        val yearRect = Rect(
            (pageLeftPadding + dateWithMonthRect.width()).toInt(),
            (pageTopPadding).toInt(),
            dateWithMonthRect.width().plus(onlyYearRect.width()).plus(calendarYearTextSpace)
                .plus(pageLeftPadding.toInt()),
            (pageTopPadding + dairyTextSize).toInt()
        )

        FTDairyDayPageRect.yearPageRect.add(yearRect)

        canvas.drawText(mDate, pageLeftPadding, pageTopPadding + dairyTextSize, dayPaint)
        canvas.drawRect(FTDairyDayPageRect().yearTextRect, FTDairyTextPaints.coloredDayRectBoxPaint)
        canvas.drawText(
            year,
            pageLeftPadding + calendarYearTextSpace + dateWithMonthRect.width().toFloat(),
            pageTopPadding + dairyTextSize,
            yearPaint
        )

        var xPosition = (canvas.width / 2).toFloat()
        var yPosition = heightPercent * 12.08f

        val item = FTScreenUtils.pickRandomQuote(context, quotesList)

        if (info.isLandscape) {
            yPosition = pageTopPadding - dairyTextSize
            drawMultiLineQuote(
                item.quote,
                (canvas.width).toFloat(),
                yPosition,
                20
            )
            yPosition = (heightPercent * 1.9f) + quoteTextHeight + pageTopPadding
            FTScreenUtils.drawCenterText(
                context,
                canvas.width.toFloat() - pageLeftPadding * 3,
                yPosition,
                canvas,
                "-" + item.author,
                18, scaleFactor
            )
        } else {
            drawMultiLineQuote(
                item.quote,
                canvas.width.toFloat(),
                yPosition,
                20
            )
            yPosition = (heightPercent * 15.03f) + quoteTextHeight
            FTScreenUtils.drawCenterText(
                context,
                xPosition,
                yPosition,
                canvas,
                "-" + item.author,
                18,
                scaleFactor
            )

        }
        mTop = if (!info.isLandscape) {
            heightPercent * 22.41f
        } else {
            heightPercent * 18.61f
        }
        dailyQuestionnaire(canvas, "My affirmations for the day")
        dailyQuestionnaire(canvas, "Today I will accomplish")
        dailyQuestionnaire(canvas, "I am thankful for")
        val rectPaint = Paint()
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = context.resources.getColor(R.color.box_background, context.theme)
        mTop += if (!info.isLandscape) {
            4.08f * heightPercent
        } else {
            4.86f * heightPercent
        }
        canvas.drawRect(
            RectF(
                0f, mTop, info.screenSize.width.toFloat(), info.screenSize.height
                    .toFloat()
            ), rectPaint
        )
        dailyQuestionnaire(canvas, "Three things that made me happy today")
        dailyQuestionnaire(canvas, "Today I learnt")
        document.finishPage(page)
    }

    private fun dailyQuestionnaire(canvas: Canvas?, question: String) {
        FTDairyTextPaints.dairyText_Paint.textSize = 20 * scaleFactor
        mTop += if (!info.isLandscape /*&& orientation != Configuration.ORIENTATION_LANDSCAPE*/) {
            pageTopPadding + dairyTextSize
        } else {
            pageTopPadding
        }
        canvas!!.drawText(question, pageLeftPadding, mTop, FTDairyTextPaints.dairyText_Paint)
        var fgPaintSel: Paint
        for (i in 1..maxLines) {
            mTop = mTop + templateDottedLineGap
            mBottom = mBottom + heightPercent * 2.91f
            fgPaintSel = Paint()
            fgPaintSel.setARGB(255, 0, 0, 0)
            fgPaintSel.style = Paint.Style.STROKE
            fgPaintSel.pathEffect = DashPathEffect(floatArrayOf(6f, 3f), 0f)
            canvas.drawLine(
                pageLeftPadding,
                mTop,
                info.screenSize.width - pageLeftPadding,
                mTop,
                fgPaintSel
            )
        }
    }

    private fun drawMultiLineText(text: String, dX: Float, dy: Float, isBulletPoint: Boolean) {
        val pointTextSize = (25 * scaleFactor)
        val textPaint = TextPaint().apply {
            letterSpacing = 0.025f
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_regular)
            color = context.resources.getColor(R.color.text_color, context.theme)
        }
        val textWidth: Float
        if (isBulletPoint)
            textWidth = info.screenSize.width - (14.25f * widthPercent)
        else
            textWidth = info.screenSize.width - (16.25f * widthPercent)

        val staticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            textWidth.toInt()
        ).setLineSpacing(0f, 1.25f).build()
        canvas.save()
        val dx = if (isBulletPoint) {
            dX
        } else {
            if (info.isLandscape) {
                introPageLeftMargin
            } else
                4.16f * widthPercent
        }
        canvas.translate(dx, dy)
        staticLayout.draw(canvas)
        canvas.restore()

        this@FTDayAndNightJournal.dy += staticLayout.height.toFloat() + 1.25f * heightPercent

    }

    private fun drawMultiLineQuote(
        text: String,
        dx: Float,
        dy: Float,
        quoteTextSize: Int
    ) {
        val pointTextSize = (quoteTextSize * scaleFactor)
        val textPaint = TextPaint().apply {
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_italic)
//            color = Color.GRAY
            color = context.resources.getColor(R.color.dotted_line_color)
            textAlign = Paint.Align.LEFT
        }
        var staticLayout: StaticLayout?
        if (info.isLandscape) {
            staticLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                (dx / 2).toInt() - pageLeftPadding.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                .setLineSpacing(0f, 1.25f).build()
            canvas.save()
            canvas.translate((info.screenSize.width / 2).toFloat(), dy)
        } else {
            staticLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                dx.toInt() - (pageLeftPadding * 2).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.25f).build()
            canvas.save()
            canvas.translate(pageLeftPadding, dy)

        }
        staticLayout.draw(canvas)
        canvas.restore()
        quoteTextHeight = staticLayout.height
    }

    private fun drawBullet(dy: Float) {
        val dx = 8.125f * widthPercent
        val pointTextSize = (25 * scaleFactor)
        val textPaint = TextPaint().apply {
            letterSpacing = 0.025f
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_regular)
            color = context.resources.getColor(R.color.text_color, context.theme)
        }

        val staticLayout = StaticLayout.Builder.obtain(
            "• ",
            0,
            "• ".length,
            textPaint,
            (info.screenSize.width - dx).toInt()
        ).setLineSpacing(0f, 1.25f).build()
        canvas.save()
        canvas.translate(dx, dy)
        staticLayout.draw(canvas)
        canvas.restore()
    }


    private fun findBoxHeight(rows: Int): Float {
        /*  The following are the major factors here
        Height and Width
        info.isLandscape
        No of Rows
        No of columns
        left margins & right margins for either orientations
        box vertical spacing
        box horizontal spacing*/
        val leftOverSpace =
            info.screenSize.height - calendarTopManualSpace - pageBottomPadding - (rows - 1) * calendarVerticalSpacing
        calendarBoxHeight = leftOverSpace / rows
        return calendarBoxHeight
    }

    private fun findBoxWidth(columns: Int): Float {
        /*The following are the major factors here
        Height and Width
        info.isLandscape
        No of Rows
        No of columns
        left margins & right margins for either orientations
        box vertical spacing
        box horizontal spacing*/
        val leftOverSpace =
            info.screenSize.width - (2 * pageLeftPadding) - (columns - 1) * calendarHorizontalSpacing
        calendarBoxWidth = leftOverSpace / columns
        return calendarBoxWidth
    }
}