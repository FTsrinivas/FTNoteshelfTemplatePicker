package com.fluidtouch.noteshelf.templatepicker.interfaces;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;

public interface AddCustomThemeListener {
    void onTemplateSelect(FTNTheme theme,boolean landscapeStatus);
    void onTemplateDelete(FTNTheme theme);
}
