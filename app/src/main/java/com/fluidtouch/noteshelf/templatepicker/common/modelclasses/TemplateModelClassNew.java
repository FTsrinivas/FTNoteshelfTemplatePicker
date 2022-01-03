package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

import android.content.Context;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;

public class TemplateModelClassNew {

    public FTNTheme ftnTheme;
    public Context mContext;

    public FTTemplateDetailedInfoAdapter getFtTemplateDetailedInfoAdapter() {
        return ftTemplateDetailedInfoAdapter;
    }

    public void setFtTemplateDetailedInfoAdapter(FTTemplateDetailedInfoAdapter ftTemplateDetailedInfoAdapter) {
        this.ftTemplateDetailedInfoAdapter = ftTemplateDetailedInfoAdapter;
    }

    public FTTemplateDetailedInfoAdapter ftTemplateDetailedInfoAdapter;
    public FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder;

    public FTNTheme getFtnTheme() {
        return ftnTheme;
    }

    public void setFtnTheme(FTNTheme ftnTheme) {
        this.ftnTheme = ftnTheme;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public FTTemplateDetailedInfoAdapter.ThemeViewHolder getChildViewHolder() {
        return childViewHolder;
    }

    public void setChildViewHolder(FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder) {
        this.childViewHolder = childViewHolder;
    }



}
