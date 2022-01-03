package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.preferences.IndividualDocumentPref;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FTDocumentDigitalDiaryPostOperation implements FTDocumentPostProcessOperation.FTPostProcess {
    private FTUrl fileURL;
    private FTDocumentInputInfo documentInfo;

    FTDocumentDigitalDiaryPostOperation(FTUrl url, FTDocumentInputInfo info) {
        this.fileURL = url;
        this.documentInfo = info;
    }

    @Override
    public void perform() {
        int pageNumber = pageNumberForCurrentDate(new Date());
        FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(this.fileURL);
        new IndividualDocumentPref().init(document.getDocumentUUID()).save("currentPage", pageNumber);
    }

    public int pageNumberForCurrentDate(Date currentDate) {
        FTDocumentInputInfo.FTPostProcessInfo postProcessInfo = this.documentInfo.postProcessInfo;
        if (postProcessInfo.offsetCount == 0) return 0;
        Calendar day = new GregorianCalendar();
        day.setTime(currentDate);
        Calendar startDate = new GregorianCalendar();
        startDate.setTime(postProcessInfo.startDate);
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        Calendar endDate = new GregorianCalendar();
        endDate.setTime(postProcessInfo.endDate);
        endDate.set(endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (day.get(Calendar.MONTH) == startDate.get(Calendar.MONTH) && day.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                || (day.get(Calendar.MONTH) == endDate.get(Calendar.MONTH) && day.get(Calendar.YEAR) == endDate.get(Calendar.YEAR))
                || (day.after(startDate) && day.before(endDate))) {
            return postProcessInfo.offsetCount + daysBetween(startDate.getTime(), currentDate);
        }
        return 0;
    }

    private int daysBetween(Date day1, Date day2) {
        Calendar d1 = new GregorianCalendar();
        d1.setTime(day1);
        Calendar d2 = new GregorianCalendar();
        d2.setTime(day2);

        Calendar dayOne = (Calendar) d1.clone(),
                dayTwo = (Calendar) d2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }
}
