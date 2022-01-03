package com.fluidtouch.dynamicgeneration

import android.content.Context
import android.util.Log
import android.util.Size
import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface
import com.fluidtouch.dynamicgeneration.templateformatsnew.FTDynamicTemplateFormat
import com.fluidtouch.noteshelf.documentframework.FTUrl
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme
import com.dd.plist.NSDictionary
class FTDynamicTemplateGenerator(templateInfoDict: NSDictionary,
                                 isLandscape: Boolean,
                                 screenSize: Size,
                                 context: Context) {

    private val context: Context = context

    fun generate(theme: FTNDynamicTemplateTheme): FTUrl {
        var path: String?= null
        val ftDynamicTemplateFormat = FTDynamicTemplateFormat(theme)
        Log.d("TemplatePicker==>", "getClassInstance::-" + theme.themeClassName);
        val temp: TemplatesGeneratorInterface = ftDynamicTemplateFormat.getClassInstance(theme.themeClassName)
        path = temp.generateTemplate(theme, context)
        Log.d("TemplatePicker==>", "getClassInstance path::-" + path);
        return FTUrl(path)
    }

}