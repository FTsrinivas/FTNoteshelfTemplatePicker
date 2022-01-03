package com.fluidtouch.noteshelf.templatepicker.common.util;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;

public class FTRecentlyDeletedTemplateInfo {
    String themeName;
    String themeURL;

    public String getDefaultThemeStatus() {
        return defaultThemeStatus;
    }

    public void setDefaultThemeStatus(String defaultThemeStatus) {
        this.defaultThemeStatus = defaultThemeStatus;
    }

    String defaultThemeStatus;

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeURL() {
        return themeURL;
    }

    public void setThemeURL(String themeURL) {
        this.themeURL = themeURL;
    }
}
