package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import com.fluidtouch.noteshelf.document.enums.FTCoverOverlayStyle;
import com.fluidtouch.noteshelf.document.enums.FTPageFooterOption;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;

import java.util.Date;
import java.util.HashMap;

public class FTDocumentInputInfo {

    public FTUrl inputFileURL;
    public Boolean isTemplate = false;
    public Boolean isFromAssets = false;
    public FTPageFooterOption footerOption = FTPageFooterOption.HIDE;

    public Integer insertAt = 0;
    public Boolean isImageSource = false;
    public Boolean isNewBook = false;

    public Integer lineHeight = 34;

    public FTCoverOverlayStyle overlayStyle = FTCoverOverlayStyle.DEFAULT_STYLE;
    public FTDocumentPin pin;

    FTNCoverTheme coverTheme;

    HashMap annotationInfo;

    public FTPostProcessInfo postProcessInfo = new FTPostProcessInfo();

    public FTNCoverTheme getCoverTheme() {
        return coverTheme;
    }

    public void setCoverTheme(FTNCoverTheme coverTheme) {
        this.coverTheme = coverTheme;
    }

    public static class FTPostProcessInfo {
        public int diaryStartYear;
        public Date startDate;
        public Date endDate;
        public int offsetCount = 76;
        public FTDocumentType documentType = FTDocumentType.defaultType;
    }
}
