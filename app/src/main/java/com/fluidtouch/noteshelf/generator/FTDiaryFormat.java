package com.fluidtouch.noteshelf.generator;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.generator.formats.FTDiaryFormat2020;
import com.fluidtouch.noteshelf.generator.formats.FTModernDiaryFormat;
import com.fluidtouch.noteshelf.generator.formats.dayandnight_journal.FTDayAndNightJournal;
import com.fluidtouch.noteshelf.generator.models.info.FTDayInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTMonthlyCalendarInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTWeekInfo;
import com.fluidtouch.noteshelf.generator.models.info.FTYearFormatInfo;
import com.fluidtouch.noteshelf.generator.models.info.rects.FTDiaryRectsInfo;
import com.fluidtouch.noteshelf.generator.models.screenInfo.FTScreenInfo;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FTDiaryFormat implements FTDairyRenderFormat {
    private final Context context;
    public PdfDocument document;

    protected FTDiaryRectsInfo.FTDiaryYearRectsInfo yearRectsInfo = new FTDiaryRectsInfo.FTDiaryYearRectsInfo();
    protected List<FTDiaryRectsInfo.FTDiaryMonthRectsInfo> monthRectsInfos = new ArrayList<>();
    protected List<FTDiaryRectsInfo.FTDiaryWeekRectsInfo> weekRectsInfos = new ArrayList<>();
    protected List<FTDiaryRectsInfo.FTDiaryDayRectsInfo> dayRectsInfos = new ArrayList<>();

    protected FTScreenInfo screenInfo;
    protected FTYearFormatInfo info;

    public static FTDiaryFormat getFormat(Context context, FTYearFormatInfo info) {
        //Template type
        if (info.templateId.equals("Modern")) {
//            return new FTDiaryFormat2020(context, info);
            return new FTModernDiaryFormat(context, info);
        } else if (info.templateId.equals("DayAndNight")) {
            return new FTDayAndNightJournal(context, info);
        }
        return null;
    }

    public FTDiaryFormat(Context context, FTYearFormatInfo info) {
        this.context = context;
        this.info = info;
        this.screenInfo = new FTScreenInfo(context, info.templateId, info.isLandscape, info.isTablet);

        String deviceName = FTApp.getPref().get(SystemPref.LAST_SELECTED_PAPER, "A4 8.3 x 11.7\"\"");
        if (deviceName.equalsIgnoreCase("This Device")) {
            this.info.screenSize = new Size(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
            this.info.screenSize = getPageSizeV2(context, this.info.screenSize, info.isLandscape);
        } else {
            this.info.screenSize = new Size(info.screenSize.getWidth(), info.screenSize.getHeight());
            this.info.screenSize = getPageSize();
        }
        Log.d("infox", "width = " + info.screenSize.getWidth() + "\theight = " + info.screenSize.getHeight());

    }

    protected PdfDocument.Page getPage(int at) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(info.screenSize.getWidth(), info.screenSize.getHeight(), at).create();
        return document.startPage(pageInfo);
    }

    protected void finishPage(PdfDocument.Page page) {
        document.finishPage(page);
    }

    private Size getPageSize() {
        float width;
        float height;
        int navigationBarHeight = getNavigationBarHeight(context);

        final boolean isTablet = ScreenUtil.isTablet();
        final int statusBarHeight = getStatusBarHeight(context);
        final int orientation = info.isLandscape ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
        final float toolbarHeight = getToolbarHeight(context, info.isLandscape, isTablet);
        final boolean isNotchDisplay = isNotchDisplay(statusBarHeight);

        if (info.screenSize.getHeight() % 10 == 0) {
            navigationBarHeight = 0;
        }
        if (isTablet) {
            if (!info.isLandscape) {
                width = Math.min(info.screenSize.getWidth(), info.screenSize.getHeight())
                        + ((orientation == Configuration.ORIENTATION_LANDSCAPE) ? navigationBarHeight : 0);
                height = Math.max(info.screenSize.getWidth(), info.screenSize.getHeight()) - toolbarHeight
                        + ((orientation == Configuration.ORIENTATION_LANDSCAPE) ? 0 : navigationBarHeight);
            } else {
                width = Math.max(info.screenSize.getWidth(), info.screenSize.getHeight())
                        + ((orientation == Configuration.ORIENTATION_PORTRAIT) ? navigationBarHeight : 0);
                height = Math.min(info.screenSize.getWidth(), info.screenSize.getHeight()) - toolbarHeight + ((orientation == Configuration.ORIENTATION_PORTRAIT) ? 0 : navigationBarHeight);
            }
        } else {
            if (!info.isLandscape) {
                width = Math.min(info.screenSize.getWidth(), info.screenSize.getHeight());
                height = Math.max(info.screenSize.getWidth(), info.screenSize.getHeight()) - toolbarHeight + ((isNotchDisplay) ? statusBarHeight : 0);
            } else {
                width = Math.max(info.screenSize.getWidth(), info.screenSize.getHeight()) + ((isNotchDisplay) ? statusBarHeight : 0);
                height = Math.min(info.screenSize.getWidth(), info.screenSize.getHeight()) - toolbarHeight;
            }
        }
        Log.d("infox", "width = " + info.screenSize.getWidth() + "\theight = " + info.screenSize.getHeight() + "\tTemplate Landscape = " + info.isLandscape + "\tDevice Landscape = " + (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));

        return new Size((int) width, (int) height);
    }

    private Size getPageSizeV2(Context context, Size screenSize, boolean isLandscape) {
        float width = screenSize.getWidth();
        float height = screenSize.getHeight();
        boolean isTablet = ScreenUtil.isTablet();
        int statusBarHeight = getStatusBarHeight(context);
//        val offset = statusBarHeight + toolbarHeight
        int orientation = Resources.getSystem().getConfiguration().orientation;
        //final int orientation = info.isLandscape ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;

        float toolbarHeight = getToolbarHeight(context, isLandscape, isTablet);
        float offset = toolbarHeight;
        int navigationBarHeight = getNavigationBarHeight(context);
        boolean isNotchDisplay = isNotchDisplay(statusBarHeight);

        if (screenSize.getHeight() % 10 == 0) {
            navigationBarHeight = 0;
        }

        if (isTablet) {
            if (!isLandscape) {
                width = Math.min(screenSize.getWidth(), screenSize.getHeight())
                        + ((orientation == Configuration.ORIENTATION_LANDSCAPE) ? navigationBarHeight : 0);
                height = Math.max(screenSize.getWidth(), screenSize.getHeight()) - offset
                        + ((orientation == Configuration.ORIENTATION_LANDSCAPE) ? 0 : navigationBarHeight);
            } else {
                width = Math.max(screenSize.getWidth(), screenSize.getHeight())
                        + ((orientation == Configuration.ORIENTATION_PORTRAIT) ? navigationBarHeight : 0);
                height = Math.min(screenSize.getWidth(), screenSize.getHeight()) - offset + ((orientation == Configuration.ORIENTATION_PORTRAIT) ? 0 : navigationBarHeight);
            }
        } else {
            if (!isLandscape) {
                width = Math.min(screenSize.getWidth(), screenSize.getHeight());
                height = Math.max(screenSize.getWidth(), screenSize.getHeight()) - offset + ((isNotchDisplay) ? statusBarHeight : 0);
            } else {
                width = Math.max(screenSize.getWidth(), screenSize.getHeight()) + ((isNotchDisplay) ? statusBarHeight : 0);
                height = Math.min(screenSize.getWidth(), screenSize.getHeight()) - offset;
            }
        }

        Log.d("infox", "width = " + info.screenSize.getWidth() + "\theight = " + info.screenSize.getHeight() + "\tTemplate Landscape = " + info.isLandscape + "\tDevice Landscape = " + (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));

        return new Size((int) width, (int) height);
    }


    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private float getToolbarHeight(Context context, boolean isLandscape, boolean isTablet) {
        float actionBarHeight = 0f;

        if (isTablet) {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        } else {
            float scale = Resources.getSystem().getDisplayMetrics().density;
            actionBarHeight = (!isLandscape) ? 56 * scale : 48 * scale;
        }

        return actionBarHeight;
    }

    private int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private boolean isNotchDisplay(int statusBarHeight) {
        return statusBarHeight > 24 * Resources.getSystem().getDisplayMetrics().density;
    }

    @Override
    public boolean isToDisplayOutOfMonthDate() {
        return false;
    }

    @Override
    public RectF pageRect() {
        return null;
    }

    @Override
    public void setDocument(PdfDocument document) {
        this.document = document;
    }

    @Override
    public void renderYearPage(Context context, List<FTMonthInfo> months, FTYearFormatInfo calendarYear) {

    }

    @Override
    public void renderMonthPage(Context context, FTMonthlyCalendarInfo monthInfo, FTYearFormatInfo calendarYear) {

    }

    @Override
    public void renderWeekPage(Context context, FTWeekInfo weeklyInfo) {

    }

    @Override
    public void renderDayPage(Context context, FTDayInfo dayInfo) {

    }

    protected int getColumnCount() {
        return info.isLandscape ? 3 : 4;
    }

    protected int getRowCount() {
        return info.isLandscape ? 4 : 3;
    }

    protected void drawRectangle(PdfDocument.Page page, RectF rect) {
        Paint paint = new Paint();
        paint.setColor(context.getColor(R.color.box_color));
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        page.getCanvas().drawRect(rect, paint);
    }

    protected Rect drawText(PdfDocument.Page page, String text, RectF rect, float textSize, int color, Locale locale) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextSize(textSize);
        textPaint.setColor(context.getColor(color));
        textPaint.setTextLocale(locale);
        Rect textBoundingRect = new Rect();

        textPaint.getTextBounds(text, 0, text.length(), textBoundingRect);
        StaticLayout mTextLayout = new StaticLayout(text, textPaint, page.getCanvas().getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        page.getCanvas().save();

        page.getCanvas().translate(rect.left, rect.top);
        mTextLayout.draw(page.getCanvas());
        page.getCanvas().restore();
        textPaint = null;
        return textBoundingRect;
    }

    protected void drawLine(PdfDocument.Page page, float startX, float startY, float stopX, float stopY) {
        Paint paint = new Paint();
        paint.setColor(context.getColor(R.color.line_color));
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        page.getCanvas().drawLine(startX, startY, stopX, stopY, paint);
    }

    protected Rect getTextRect(String text, float textSize, Locale locale) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextSize(textSize);
        textPaint.setTextLocale(locale);
        Rect textBoundingRect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBoundingRect);
        return textBoundingRect;
    }

    public boolean isBelongToCalendarYear(FTDayInfo dayInfo) {
        Calendar day = new GregorianCalendar(dayInfo.locale);
        day.setTime(dayInfo.date);
        Calendar startDate = new GregorianCalendar(dayInfo.locale);
        startDate.setTime(info.startMonth);
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        Calendar endDate = new GregorianCalendar(dayInfo.locale);
        endDate.setTime(info.endMonth);
        endDate.set(endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        return (day.get(Calendar.MONTH) == startDate.get(Calendar.MONTH) && day.get(Calendar.YEAR) == startDate.get(Calendar.YEAR))
                || (day.get(Calendar.MONTH) == endDate.get(Calendar.MONTH) && day.get(Calendar.YEAR) == endDate.get(Calendar.YEAR))
                || (day.after(startDate) && day.before(endDate));
    }
}
