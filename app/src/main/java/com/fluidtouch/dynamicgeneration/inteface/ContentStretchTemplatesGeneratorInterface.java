package com.fluidtouch.dynamicgeneration.inteface;

import android.content.Context;

import com.fluidtouch.noteshelf.models.theme.FTNContentStretchTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;

public interface ContentStretchTemplatesGeneratorInterface {
    String generateTemplate(FTNContentStretchTemplateTheme theme, Context mContex);
}
