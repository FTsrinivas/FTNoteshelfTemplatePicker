package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

import android.content.Context;
import android.util.Size;

import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;

public class TemplateModelClass {

    protected FTNDynamicTemplateTheme ftnTheme;
    protected boolean isLandscape;
    protected Size templateSize;
    protected Context mContext;

    public FTNDynamicTemplateTheme getFtnTheme() {
        return ftnTheme;
    }

    public void setFtnTheme(FTNDynamicTemplateTheme ftnTheme) {
        this.ftnTheme = ftnTheme;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }

    public Size getTemplateSize() {
        return templateSize;
    }

    public void setTemplateSize(Size templateSize) {
        this.templateSize = templateSize;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }



}
