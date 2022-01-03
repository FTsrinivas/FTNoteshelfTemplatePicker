package com.fluidtouch.noteshelf.generator;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;

import com.fluidtouch.noteshelf.generator.models.info.FTDayInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthlyCalendarInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTWeekInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo;

import java.util.List;

public interface FTDairyRenderFormat {
    boolean isToDisplayOutOfMonthDate();

    RectF pageRect();

    void setDocument(PdfDocument document);

    void renderYearPage(Context context, List<FTMonthInfo> months, FTYearFormatInfo calendarYear);

    void renderMonthPage(Context context, FTMonthlyCalendarInfo monthInfo, FTYearFormatInfo calendarYear);

    void renderWeekPage(Context context, FTWeekInfo weeklyInfo);

    void renderDayPage(Context context, FTDayInfo dayInfo);

    interface FTDairyRenderTemplate {
        String getDayTemplate();

        String getWeekTemplate();

        String getMonthTemplate();

        String getYearTemplate();
    }
}
