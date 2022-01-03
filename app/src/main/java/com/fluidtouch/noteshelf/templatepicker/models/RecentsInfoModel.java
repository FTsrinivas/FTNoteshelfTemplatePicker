package com.fluidtouch.noteshelf.templatepicker.models;

import android.graphics.Bitmap;

public class RecentsInfoModel {

    public String themeBgClrName        = "White";
    public String themeBgClr            = "#F7F7F2-1.0";
    public String horizontalLineColor   = "#000000-0.5";

    public String getThemeBgClrName() {
        return themeBgClrName;
    }

    public void setThemeBgClrName(String themeBgClrName) {
        this.themeBgClrName = themeBgClrName;
    }

    public String getThemeBgClr() {
        return themeBgClr;
    }

    public void setThemeBgClr(String themeBgClr) {
        this.themeBgClr = themeBgClr;
    }

    public String getHorizontalLineColor() {
        return horizontalLineColor;
    }

    public void setHorizontalLineColor(String horizontalLineColor) {
        this.horizontalLineColor = horizontalLineColor;
    }

    public String getVerticalLineColor() {
        return verticalLineColor;
    }

    public void setVerticalLineColor(String verticalLineColor) {
        this.verticalLineColor = verticalLineColor;
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }

    public String verticalLineColor     = "#000000-0.5";
    public int horizontalSpacing        = 34;
    public int verticalSpacing          = 34;
    
    boolean isLandscape;

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }

    public String get_dairyStartDate() {
        return _dairyStartDate;
    }

    public void set_dairyStartDate(String _dairyStartDate) {
        this._dairyStartDate = _dairyStartDate;
    }

    public String get_dairyEndDate() {
        return _dairyEndDate;
    }

    public void set_dairyEndDate(String _dairyEndDate) {
        this._dairyEndDate = _dairyEndDate;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    String _dairyStartDate;
    String _dairyEndDate;

    float width                  = 595.0f;
    float height                 = 842.0f;

    String _packName;

    public String get_themeName() {
        return _themeName;
    }

    public void set_themeName(String _themeName) {
        this._themeName = _themeName;
    }

    String _themeName;

    public RecentsInfoModel() {
    }

    public String get_packName() {
        return _packName;
    }

    public void set_packName(String _packName) {
        this._packName = _packName;
    }

    public String get_categoryName() {
        return _categoryName;
    }

    public void set_categoryName(String _categoryName) {
        this._categoryName = _categoryName;
    }

    public String get_thumbnailURLPath() {
        return _thumbnailURLPath;
    }

    public void set_thumbnailURLPath(String _thumbnailURLPath) {
        this._thumbnailURLPath = _thumbnailURLPath;
    }

    String _categoryName;
    String _thumbnailURLPath;


    public String get_themeBitmapInStringFrmt() {
        return _themeBitmapInStringFrmt;
    }

    public void set_themeBitmapInStringFrmt(String _themeBitmapInStringFrmt) {
        this._themeBitmapInStringFrmt = _themeBitmapInStringFrmt;
    }

    String _themeBitmapInStringFrmt;

}
