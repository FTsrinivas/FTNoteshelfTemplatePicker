package com.fluidtouch.dynamicgeneration.inteface;

import android.content.Context;

import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;

public interface TemplatesGeneratorInterface {
    String generateTemplate(FTNDynamicTemplateTheme theme, Context mContex);
}
