package com.fluidtouch.noteshelf.templatepicker.models;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;

import java.util.ArrayList;

public class TemplatesInfoModel {

    public boolean isCustomizeOptions() {
        return customizeOptions;
    }

    public void setCustomizeOptions(boolean customizeOptions) {
        this.customizeOptions = customizeOptions;
    }

    boolean customizeOptions;

    public String get_categoryName() {
        return _categoryName;
    }

    public void set_categoryName(String _categoryName) {
        this._categoryName = _categoryName;
    }

    public ArrayList<FTNTheme> get_themeseList() {
        return _themeseList;
    }

    public void set_themeseList(ArrayList<FTNTheme> _themeseList) {
        this._themeseList = _themeseList;
    }

    public void AddThemesToList(FTNTheme _theme) {
        this._themeseList.add(_theme);
    }

    String _categoryName;
    ArrayList<FTNTheme> _themeseList = new ArrayList<>();
}
