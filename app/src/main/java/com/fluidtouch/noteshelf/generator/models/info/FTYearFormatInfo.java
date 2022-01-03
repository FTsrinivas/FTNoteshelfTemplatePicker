package com.fluidtouch.noteshelf.generator.models.info;

import android.content.Context;
import android.util.Size;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FTYearFormatInfo {
    public static String calendarYearHeading;
    public FTDayFormatInfo dayFormat = new FTDayFormatInfo();
    public Date startMonth = new Date();
    public Date endMonth = new Date();
    public Locale locale = FTApp.getInstance().getResources().getConfiguration().getLocales().get(0);
    private final int mPageWidth = Math.round(FTSelectedDeviceInfo.selectedDeviceInfo().getPageWidth());
    private final int mPageHeight = Math.round(FTSelectedDeviceInfo.selectedDeviceInfo().getPageHeight());
    public Size screenSize = new Size(mPageWidth, mPageHeight);
    public boolean isTablet = false;
    public String templateId = "Modern";
    public boolean isLandscape;
    public String weekFormat = String.valueOf(new GregorianCalendar(locale).getFirstDayOfWeek());


    public FTYearFormatInfo(Date startDate, Date endDate, String templateId, boolean isLandscape) {
        this.startMonth = startDate;
        this.endMonth = endDate;
        this.templateId = templateId;
        this.isLandscape = isLandscape;
    }

    public static String getYearTitle(String startYear, String endYear) {
        calendarYearHeading = startYear + (startYear.equals(endYear) ? "" : "-" + endYear.substring(2, 4));
        return calendarYearHeading;
    }

}