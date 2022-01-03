package com.fluidtouch.dynamicgeneration

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme
import com.fluidtouch.noteshelf.preferences.SystemPref
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.ColorRGB
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateMoreDetailsInfo
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil
import java.lang.reflect.Constructor

open class FTDynamicTemplateFormat internal constructor() {
    /*var templateInfo: FTDynamicTemplateInfo? = null
    var currentPage: PdfDocument.Page? = null
    val scale = Resources.getSystem().displayMetrics.density

    internal constructor(templateInfo: FTDynamicTemplateInfo) : this() {
        this.templateInfo = templateInfo
    }

    companion object {
        fun getFormat(templateInfo: FTDynamicTemplateInfo): FTDynamicTemplateFormat {
            val instance = ClassFromString.getClassInstance(templateInfo.codableInfo.themeClassName, templateInfo)

            if (instance != null) {
                return instance
            }

            return FTDynamicTemplateFormat(templateInfo)
        }
    }

    open fun renderTemplate(document: PdfDocument) {
        val pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo
                .Builder(templateInfo!!.width, templateInfo!!.height, 1).create()
        currentPage = document.startPage(pageInfo)
        val canvas = currentPage!!.canvas
        canvas.drawColor(ColorUtil.getColor(templateInfo!!.codableInfo.bgColor))
    }

    open fun verticalLineCount(): Int {
        return 0
    }

    open fun horizontalLineCount(): Int {
        return 0
    }

    internal object ClassFromString {
        fun getClassInstance(className: String, templateInfo: FTDynamicTemplateInfo): FTDynamicTemplateFormat {
            //val c = Class.forName("com.fluidtouch.dynamicgeneration.templateformats." + className)
            val c = Class.forName("com.fluidtouch.dynamicgeneration.templateformatsnew." + className)
            val cons: Constructor<*> = c.getConstructor(FTDynamicTemplateInfo::class.java)
            val `object`: Any = cons.newInstance(templateInfo)

            return `object` as FTDynamicTemplateFormat
        }
    }

    protected object ColorUtil {
        internal fun getColor(hexString: String): Int {
            var string = hexString;
            if (hexString.contains("#")) {
                string = string.split("#")[1].split("-")[0]
            }
            val red = hexToDecimal(string.substring(0, 2))
            val green = hexToDecimal(string.substring(2, 4))
            val blue = hexToDecimal(string.substring(4, 6))
            val alpha = (hexString.split("-")[1].toFloat() * 255).toInt()
            return Color.argb(alpha, red, green, blue)
        }

        private fun hexToDecimal(string: String): Int {
            return Integer.parseInt(string, 16)
        }
    }*/
}