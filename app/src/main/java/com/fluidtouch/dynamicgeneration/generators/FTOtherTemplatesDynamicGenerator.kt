package com.fluidtouch.dynamicgeneration.generators

//import com.fluidtouch.dynamicgeneration.FTBasicTemplatesDynamicGenerator
import android.content.Context
import android.util.Log
import com.fluidtouch.dynamicgeneration.templateformatsnew.FTContentStretchPDF
import com.fluidtouch.noteshelf.documentframework.FTUrl
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme

class FTOtherTemplatesDynamicGenerator(context: Context) {

    private val context: Context = context

    fun generate(theme: FTNDynamicTemplateTheme): FTUrl {
        var path: String?= null
        Log.d("TemplatePicker==>", "getClassInstance theme.dynamicId == 3::-" + theme);
        val ftContentStretchPDF = FTContentStretchPDF(theme);
        path = ftContentStretchPDF.generateTemplate(theme,context)
        return FTUrl(path)
    }

}