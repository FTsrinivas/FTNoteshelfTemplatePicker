package com.fluidtouch.noteshelf.generator.formats;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.util.Log;
import android.util.SizeF;

import com.fluidtouch.noteshelf.commons.utils.FTScreenUtils;
import com.fluidtouch.noteshelf.generator.FTDiaryFormat;
import com.fluidtouch.noteshelf.generator.models.info.FTDayInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthlyCalendarInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTWeekInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo;
import com.fluidtouch.noteshelf.generator.models.info.rects.FTDiaryRectsInfo;
import com.fluidtouch.noteshelf.generator.models.screenInfo.FTScreenFontsInfo;
import com.fluidtouch.noteshelf.generator.models.screenInfo.FTScreenSpacesInfo;
import com.fluidtouch.noteshelf2.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class FTModernDiaryFormat extends FTDiaryFormat {
    private final float heightPercent;
    private final float widthPercent;
    int mPageWidth;
    int mPageHeight;
    float density;


    public FTModernDiaryFormat(Context context, FTYearFormatInfo info) {
        super(context, info);
        mPageWidth = info.screenSize.getWidth();
        mPageHeight = info.screenSize.getHeight();
        heightPercent = mPageHeight / 100f;
        widthPercent = mPageWidth / 100f;

        SizeF thSize = new SizeF(info.screenSize.getWidth(), info.screenSize.getHeight());
        SizeF aspectSize = FTScreenUtils.INSTANCE.aspectSize(thSize, new SizeF(800f, 1200f));
        if (info.isLandscape) {
            aspectSize = FTScreenUtils.INSTANCE.aspectSize(thSize, new SizeF(1200f, 800f));
        }
        density = thSize.getWidth() / aspectSize.getWidth();
//        density = context.getResources().getDisplayMetrics().density;
        Log.d("##FTModernDiaryFormat", "ScaleFactor " + density + "\n width x Height" + mPageWidth + " x " + mPageHeight);

    }

    @Override
    public void renderYearPage(Context context, List<FTMonthInfo> months, FTYearFormatInfo calendarYear) {
        super.renderYearPage(context, months, calendarYear);
        PdfDocument.Page page = getPage(0);

        float leftMargin = 5.15f * widthPercent;
        float topMargin = calendarYear.isLandscape ? 11.68f * heightPercent : 10.78f * heightPercent;
        float bottomMargin = 3.81f * heightPercent;
        float horizontalSpace = 3.59f * widthPercent;
        float verticalSpace = 2.86f * heightPercent;

        RectF yearRect = new RectF(leftMargin, topMargin, 0, 0);

        Calendar startCalendar = new GregorianCalendar(info.locale);
        startCalendar.setTime(info.startMonth);
        String startYear = String.valueOf(startCalendar.get(Calendar.YEAR));
        Calendar endCalendar = new GregorianCalendar(info.locale);
        endCalendar.setTime(info.endMonth);
        String endYear = String.valueOf(endCalendar.get(Calendar.YEAR));
        Rect yearBoundRect = drawText(page, startYear + (startYear.equals(endYear) ? "" : "-" + endYear.substring(2, 4)), yearRect, 150.0f * density, R.color.charcoal_gray, info.locale);


        FTScreenFontsInfo.FTScreenYearFontsInfo fontsInfo = screenInfo.fontsInfo.yearFontsInfo;
        int columnCount = getColumnCount();
        int rowCount = getRowCount();
        float yearTextOffsetY = topMargin + (/*2 **/ bottomMargin) + yearBoundRect.height();
        float boxWidth = (mPageWidth - ((columnCount - 1) * horizontalSpace) - (2 * leftMargin)) / columnCount;
        float boxHeight = (mPageHeight - ((rowCount - 1) * verticalSpace) - yearTextOffsetY - (2 * bottomMargin)) / rowCount;

        final float monthTitleOffsetX = 6.1f * density;
        final float monthTitleOffsetY = 5.8f * density;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float monthCellLeft = leftMargin + (boxWidth * col) + (col * horizontalSpace);
                float monthCellTop = yearTextOffsetY + (bottomMargin) + (row * boxHeight) + (row * verticalSpace);
                RectF monthCellRect = new RectF(monthCellLeft, monthCellTop, monthCellLeft + boxWidth, monthCellTop + boxHeight);
                drawRectangle(page, monthCellRect);

                int index = columnCount * row + col;
                if (index < months.size()) {
                    String monthTitle = months.get(index).monthTitle;
                    RectF monthRect = new RectF();
                    monthRect.left = monthCellRect.left + monthTitleOffsetX;
                    monthRect.top = monthCellRect.top + monthTitleOffsetY;
                    Rect monthTitleBoundingRect = drawText(page, monthTitle, monthRect, 20 * density, R.color.charcoal_gray, info.locale);

                    RectF monthLinkRect = new RectF();
                    monthLinkRect.left = monthCellRect.left;
                    monthLinkRect.top = monthCellRect.top;
                    monthLinkRect.right = monthLinkRect.left + monthTitleBoundingRect.width() + 30;
                    monthLinkRect.bottom = monthLinkRect.top + monthTitleBoundingRect.height() + 30;
                    yearRectsInfo.monthRects.add(monthLinkRect);
                }
            }
        }

        yearRectsInfo.yearRect.left = yearRect.left;
        yearRectsInfo.yearRect.top = yearRect.top;
        yearRectsInfo.yearRect.right = yearRectsInfo.yearRect.left + yearBoundRect.width();
        yearRectsInfo.yearRect.bottom = yearRectsInfo.yearRect.top + yearBoundRect.height();

        finishPage(page);
    }

    @Override
    public void renderMonthPage(Context context, FTMonthlyCalendarInfo monthInfo, FTYearFormatInfo calendarYear) {
        super.renderMonthPage(context, monthInfo, calendarYear);
        PdfDocument.Page page = getPage(1);
        int columnCount = 7;
        int rowCount = 6;

        FTDiaryRectsInfo.FTDiaryMonthRectsInfo monthRectsInfo = new FTDiaryRectsInfo.FTDiaryMonthRectsInfo();
        boolean isLandscape = calendarYear.isLandscape;

        float pageLeftPadding = isLandscape ? 5.19f * widthPercent : 5.04f * widthPercent;
        float pageTopPadding = isLandscape ? 20.64f * heightPercent : 10.59f * heightPercent;
        float pageBottomPadding = isLandscape ? 22.20f * heightPercent : 3.81f * heightPercent;
        float calendarOffsetY = isLandscape ? 24.6f * heightPercent : 38.16f * heightPercent;

        Rect monthBoundRect = getTextRect(monthInfo.shortMonth, 120 * density, monthInfo.locale);
        RectF monthRect = new RectF();
        monthRect.left = pageLeftPadding;
        monthRect.top = pageTopPadding;
        monthBoundRect = drawText(page, monthInfo.shortMonth.toUpperCase(monthInfo.locale), monthRect, 120 * density, R.color.charcoal_gray, monthInfo.locale);

        RectF monthLinkRect = new RectF();
        monthLinkRect.left = monthRect.left;
        monthLinkRect.top = monthRect.top;
        monthLinkRect.right = monthLinkRect.left + monthBoundRect.width();
        monthLinkRect.bottom = monthLinkRect.top + monthBoundRect.height();
        monthRectsInfo.monthRect = monthLinkRect;

        Rect yearBoundRect = getTextRect(String.valueOf(monthInfo.year), 45 * density, info.locale);
        RectF yearRect = new RectF();

        if (isLandscape) {
            yearRect.left = monthRect.left;
            yearRect.top = monthRect.top + monthBoundRect.height() + 4f * heightPercent;
        } else {
            yearRect.left = monthRect.left + monthBoundRect.width() + (yearBoundRect.width() / 2f);
            yearRect.top = monthLinkRect.bottom - yearBoundRect.height();

//            yearRect.top =  monthRect.top + monthBoundRect.height() - yearBoundRect.height();
        }

        Log.d("### yearRect.top ::  ", isLandscape + "  " + yearRect.top);

        drawText(page, monthInfo.year, yearRect, 45 * density, R.color.box_color, monthInfo.locale);

        float boxWidth = isLandscape ? (mPageWidth - (1.5f * monthBoundRect.width()) - (2 * pageLeftPadding)) / columnCount : (mPageWidth - (2 * pageLeftPadding)) / columnCount;
        float boxHeight = isLandscape ? (mPageHeight - (2 * calendarOffsetY)) / rowCount : (mPageHeight - (calendarOffsetY) - pageBottomPadding) / rowCount;


        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float rectX = isLandscape ? pageLeftPadding + (1.5f * monthBoundRect.width()) + (boxWidth * col) : pageLeftPadding + (boxWidth * col);
                float rectY = calendarOffsetY + (row * boxHeight);
                RectF dayCellRect = new RectF(rectX, rectY, rectX + boxWidth, rectY + boxHeight);
                drawRectangle(page, dayCellRect);

                int index = row * columnCount + col;
                if (index < monthInfo.dayInfos.size()) {
                    FTDayInfo dayInfo = monthInfo.dayInfos.get(index);
                    if (row == 0) {
                        Rect weekBoundRect = getTextRect(dayInfo.weekDay, 20 * density, dayInfo.locale);
                        RectF weekRect = new RectF();
                        weekRect.left = dayCellRect.left + (4.9f * 2 * density);
                        int extraHeight = weekBoundRect.height() < 10 ? 20 : 0;
                        weekRect.top = dayCellRect.top - weekBoundRect.height() - (6.5f * 2 * density) - extraHeight;
                        drawText(page, dayInfo.weekDay.toUpperCase(monthInfo.locale), weekRect, 20 * density, R.color.charcoal_gray, dayInfo.locale);
                    }
                    String date = String.valueOf(dayInfo.date.getDate());
                    RectF dayRect = new RectF();
                    dayRect.left = dayCellRect.left + (4.9f * 2 * density);
                    dayRect.top = dayCellRect.top + (3.1f * 2 * density);
                    Rect dayBoundRect = drawText(page, date, dayRect, 18 * density, dayInfo.belongsToSameMonth ? R.color.charcoal_gray : R.color.charcoal_gray_alpha_20, dayInfo.locale);

                    if (isBelongToCalendarYear(dayInfo)) {
                        RectF dayLinkRect = new RectF();
                        dayLinkRect.left = dayCellRect.left;
                        dayLinkRect.top = dayCellRect.top;
                        dayLinkRect.right = dayLinkRect.left + dayBoundRect.width() + 30;
                        dayLinkRect.bottom = dayLinkRect.top + dayBoundRect.height() + 30;
                        monthRectsInfo.dayRects.add(dayLinkRect);
                    }
                }
            }
        }


        RectF yearLinkRect = new RectF();
        yearLinkRect.left = yearRect.left;
        yearLinkRect.top = yearRect.top;
        yearLinkRect.right = yearRect.left + yearBoundRect.width() + 30;
        yearLinkRect.bottom = yearRect.top + yearBoundRect.height() + 30;
        monthRectsInfo.yearRect = yearLinkRect;

        monthRectsInfos.add(monthRectsInfo);

        finishPage(page);
    }

    @Override
    public void renderWeekPage(Context context, FTWeekInfo weeklyInfo) {
        super.renderWeekPage(context, weeklyInfo);
        PdfDocument.Page page = getPage(2);
        int columnCount = 3;
        int rowCount = 2;

        float leftPadding = 5.39f * widthPercent;
        float bottomPadding = 3.81f * heightPercent;
        float verticalMargin = 2.38f * heightPercent;
        float horizontalMargin = 3.59f * widthPercent;
        float lineYOffset = 7.44f * heightPercent;

        FTScreenFontsInfo.FTScreenWeekFontsInfo fontsInfo = screenInfo.fontsInfo.weekFontsInfo;

        float weekParentOffset = 12 * density;
        float textSize = 14 * density;

        FTDiaryRectsInfo.FTDiaryWeekRectsInfo weekRectsInfo = new FTDiaryRectsInfo.FTDiaryWeekRectsInfo();

        float lineWidth = 0;
        float lastBoxTop = 0;

        RectF dateRect = new RectF();
        dateRect.left = leftPadding;
        FTDayInfo startWeekDayInfo = weeklyInfo.dayInfos.get(0);
        String startDate = startWeekDayInfo.date.getDate() + " " + startWeekDayInfo.monthString;
        FTDayInfo endWeekDayInfo = weeklyInfo.dayInfos.get(weeklyInfo.dayInfos.size() - 1);
        String endDate = endWeekDayInfo.date.getDate() + " " + endWeekDayInfo.monthString;
        String monthTitle = startDate + " - " + endDate;
        Rect dateBoundRect = getTextRect(monthTitle, textSize, startWeekDayInfo.locale);
        dateRect.top = bottomPadding;
        Rect dateRectBounds = drawText(page, monthTitle, dateRect, textSize, R.color.black_alpha_20, weeklyInfo.locale);

        drawLine(page, leftPadding, bottomPadding + dateBoundRect.height() + 0.9f * heightPercent, leftPadding + dateRectBounds.width() + 10, bottomPadding + dateBoundRect.height() + 0.9f * heightPercent);


        RectF monthRect = new RectF();
        monthRect.left = leftPadding;
        monthRect.top = bottomPadding + dateBoundRect.height() + 1 * heightPercent;
        Rect monthBoundRect = drawText(page, startWeekDayInfo.fullMonthString.toUpperCase(weeklyInfo.locale) + " ", monthRect, textSize, R.color.black_alpha_50, weeklyInfo.locale);

        weekRectsInfo.monthRect.left = monthRect.left;
        weekRectsInfo.monthRect.top = bottomPadding + dateBoundRect.height() + 1 * heightPercent;
        weekRectsInfo.monthRect.right = monthRect.left + monthBoundRect.width();
        weekRectsInfo.monthRect.bottom = weekRectsInfo.monthRect.top + monthBoundRect.height();

        RectF yearRect = new RectF();
        yearRect.left = leftPadding + monthBoundRect.width();
        yearRect.top = bottomPadding + dateBoundRect.height() + 1 * heightPercent;
        Rect yearBoundRect = drawText(page, " " + startWeekDayInfo.yearString, yearRect, textSize, R.color.black_alpha_50, weeklyInfo.locale);

        weekRectsInfo.yearRect.left = yearRect.left;
        weekRectsInfo.yearRect.top = bottomPadding + dateBoundRect.height() + 1 * heightPercent;
        weekRectsInfo.yearRect.right = yearRect.left + yearBoundRect.width();
        weekRectsInfo.yearRect.bottom = weekRectsInfo.yearRect.top + yearBoundRect.height();

        float initialBoxYOffset = 2 * bottomPadding + dateBoundRect.height() + 2 * heightPercent + monthBoundRect.height();
        float boxWidth = (mPageWidth - (2 * leftPadding) - (2 * horizontalMargin)) / columnCount;
        float leftOverHeight = (mPageHeight - bottomPadding - initialBoxYOffset - 2 * verticalMargin);
        float lastBoxHeight = (leftOverHeight / 100) * 20;

        float boxHeight = (leftOverHeight - lastBoxHeight) / rowCount;


        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float rectX = leftPadding + (boxWidth * col) + (col * horizontalMargin);
                float rectY = initialBoxYOffset + (row * boxHeight) + (row * verticalMargin);
                RectF dayCellRect = new RectF(rectX, rectY, rectX + boxWidth, rectY + boxHeight);
                drawRectangle(page, dayCellRect);

                int index = row * columnCount + col;
                if (index < weeklyInfo.dayInfos.size()) {
                    FTDayInfo dayInfo = weeklyInfo.dayInfos.get(index);
                    RectF dayRect = new RectF();
                    dayRect.left = dayCellRect.left + weekParentOffset;
                    dayRect.top = dayCellRect.top + weekParentOffset;
                    Rect dayBoundRect = drawText(page, dayInfo.weekString.toUpperCase(weeklyInfo.locale), dayRect, textSize, R.color.black_alpha_50, dayInfo.locale);

                    if (isBelongToCalendarYear(dayInfo)) {
                        RectF dayLinkRect = new RectF();
                        dayLinkRect.left = dayCellRect.left;
                        dayLinkRect.top = dayCellRect.top;
                        dayLinkRect.right = dayLinkRect.left + dayBoundRect.width() + 30;
                        dayLinkRect.bottom = dayLinkRect.top + dayBoundRect.height() + 30;
                        weekRectsInfo.weekDayRects.add(dayLinkRect);
                    }

                    if (row == rowCount - 1 && col == columnCount - 1) {
                        lastBoxTop = dayCellRect.bottom;
                        lineWidth = dayCellRect.width() + (dayCellRect.width() / 2);
                    }
                }
            }
        }

        RectF lastBox = new RectF();
        lastBox.left = leftPadding;
        lastBox.top = lastBoxTop + verticalMargin;
        lastBox.right = mPageWidth - leftPadding;
        lastBox.bottom = lastBox.top + lastBoxHeight;
        drawRectangle(page, lastBox);

        FTDayInfo lastdayInfo = weeklyInfo.dayInfos.get(weeklyInfo.dayInfos.size() - 1);
        RectF lastDayRect = new RectF();
        lastDayRect.left = lastBox.left + weekParentOffset;
        lastDayRect.top = lastBox.top + weekParentOffset;
        Rect lastDayBoundRect = drawText(page, lastdayInfo.weekString.toUpperCase(weeklyInfo.locale), lastDayRect, textSize, R.color.black_alpha_50, weeklyInfo.locale);

        if (isBelongToCalendarYear(lastdayInfo)) {
            RectF lastDayLinkRect = new RectF();
            lastDayLinkRect.left = lastBox.left;
            lastDayLinkRect.top = lastBox.top;
            lastDayLinkRect.right = lastDayRect.left + lastDayBoundRect.width() + 30;
            lastDayLinkRect.bottom = lastDayRect.top + lastDayBoundRect.height() + 30;
            weekRectsInfo.weekDayRects.add(lastDayLinkRect);
        }

        weekRectsInfos.add(weekRectsInfo);

        finishPage(page);
    }

    @Override
    public void renderDayPage(Context context, FTDayInfo dayInfo) {
        if (!isBelongToCalendarYear(dayInfo) && !dayInfo.belongsToSameMonth) return;
        super.renderDayPage(context, dayInfo);
        PdfDocument.Page page = getPage(3);

        float pagePadding = 4.0f * widthPercent ;
        float pageTopPadding = 4.0f * heightPercent;

//        FTScreenSpacesInfo.FTScreenDaySpacesInfo spacesInfo = screenInfo.spacesInfo.daySpacesInfo;
//        FTScreenFontsInfo.FTScreenDayFontsInfo fontsInfo = screenInfo.fontsInfo.dayFontsInfo;

        FTDiaryRectsInfo.FTDiaryDayRectsInfo dayRectsInfo = new FTDiaryRectsInfo.FTDiaryDayRectsInfo();

        RectF dateRect = new RectF();
        getTextRect(dayInfo.dayString, 55*density, dayInfo.locale);
        dateRect.top = pageTopPadding;
        dateRect.left = pagePadding;
        Rect dateBoundRect = drawText(page, dayInfo.dayString, dateRect, 55*density, R.color.black_alpha_80, dayInfo.locale);

        RectF monthRect = new RectF();
        Rect monthBoundRect = getTextRect(dayInfo.fullMonthString, 18*density, dayInfo.locale);
        monthRect.top = dateRect.top + dateBoundRect.height() + monthBoundRect.height() + (monthBoundRect.height() / 2f);
        monthRect.left = pagePadding;
        drawText(page, dayInfo.fullMonthString.toUpperCase(dayInfo.locale), monthRect, 18*density, R.color.black_alpha_80, dayInfo.locale);

        dayRectsInfo.monthRect.left = monthRect.left;
        dayRectsInfo.monthRect.top = monthRect.top;
        dayRectsInfo.monthRect.right = dayRectsInfo.monthRect.left + monthBoundRect.width();
        dayRectsInfo.monthRect.bottom = dayRectsInfo.monthRect.top + monthBoundRect.height();

        RectF weekRect = new RectF();
        Rect weekBoundRect = getTextRect(dayInfo.weekString, 18*density, dayInfo.locale);
        weekRect.top = monthRect.top + monthBoundRect.height() + (weekBoundRect.height() / 2f);
        weekRect.left = pagePadding;
        drawText(page, dayInfo.weekString.toUpperCase(dayInfo.locale), weekRect,14*density, R.color.black_alpha_80, dayInfo.locale);

        dayRectsInfo.weekRect.left = weekRect.left;
        dayRectsInfo.weekRect.top = weekRect.top;
        dayRectsInfo.weekRect.right = dayRectsInfo.weekRect.left + weekBoundRect.width();
        dayRectsInfo.weekRect.bottom = dayRectsInfo.weekRect.top + weekBoundRect.height();

        RectF yearRect = new RectF();
        yearRect.top = monthRect.top + 18*density;
        Rect yearBoundRect = getTextRect(String.valueOf(dayInfo.year), 18*density, dayInfo.locale);
        yearRect.left = mPageWidth - pagePadding - yearBoundRect.width();
        drawText(page, String.valueOf(dayInfo.year), yearRect, 18*density, R.color.black_alpha_80, dayInfo.locale);

        dayRectsInfo.yearRect.left = yearRect.left - 15;
        dayRectsInfo.yearRect.top = yearRect.top - 15;
        dayRectsInfo.yearRect.right = yearRect.left + yearBoundRect.width() + 15;
        dayRectsInfo.yearRect.bottom = yearRect.top + yearBoundRect.height() + 15;

        for (float y = yearRect.top + 18*density + pagePadding; y < mPageHeight - pageTopPadding; y += pageTopPadding) {
            drawLine(page, pagePadding, y, mPageWidth - (pagePadding), y + 1);
        }

        dayRectsInfos.add(dayRectsInfo);

        finishPage(page);
    }
}
