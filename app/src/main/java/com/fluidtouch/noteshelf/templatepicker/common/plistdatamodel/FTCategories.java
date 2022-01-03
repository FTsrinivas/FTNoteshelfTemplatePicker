package com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;

import java.util.ArrayList;

public class FTCategories {

    String category_name;
    String event_track_name;
    boolean customizeOptions;
    ArrayList<FTNTheme> ftThemes;
    FTCustomizeOptions mFTCustomizeOptions;

    public FTNThemeCategory.FTThemeType getFtThemeType() {
        return ftThemeType;
    }

    public void setFtThemeType(FTNThemeCategory.FTThemeType ftThemeType) {
        this.ftThemeType = ftThemeType;
    }

    FTNThemeCategory.FTThemeType ftThemeType;

    public boolean isCustomizeOptions() {
        return customizeOptions;
    }

    public void setCustomizeOptions(boolean customizeOptions) {
        this.customizeOptions = customizeOptions;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public FTCustomizeOptions getmFTCustomizeOptions() {
        return mFTCustomizeOptions;
    }

    public void setmFTCustomizeOptions(FTCustomizeOptions mFTCustomizeOptions) {
        this.mFTCustomizeOptions = mFTCustomizeOptions;
    }

    public String getEvent_track_name() {
        return event_track_name;
    }

    public void setEvent_track_name(String event_track_name) {
        this.event_track_name = event_track_name;
    }

    public ArrayList<FTNTheme> getFtThemes() {
        return ftThemes;
    }

    public void setFtThemes(ArrayList<FTNTheme> ftThemes) {
        this.ftThemes = ftThemes;
    }
    public void addFtThemes(ArrayList<FTNTheme> ftThemes) {
        this.ftThemes.clear();
        this.ftThemes.addAll( ftThemes);
    }
}
