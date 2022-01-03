package com.fluidtouch.dynamicgeneration

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.util.Size
import android.util.TypedValue
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSObject
import com.fluidtouch.noteshelf.commons.FTLog
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min


class FTDynamicTemplateInfo(templateInfoDict: NSDictionary, isLandscape: Boolean, size: Size, context: Context) {
    var codableInfo: FTDynamicTemplateCodableInfo = FTDynamicTemplateCodableInfo(templateInfoDict, scale = Resources.getSystem().displayMetrics.density)
    var width: Int = 0
    var height: Int = 0
    val isLandscape: Boolean = isLandscape

    init {
        if (codableInfo.width == 0 && codableInfo.height == 0) {
            setTemplateSize(size, context)
        } else {
            this.width = codableInfo.width
            this.height = codableInfo.height
        }
        Log.i("Ding:after", "" + this.width + " * " + this.height)
    }

    public fun setTemplateSize(size: Size, context: Context) {
        val isTablet = ScreenUtil.isTablet();
        val statusBarHeight = getStatusBarHeight(context)
        val toolbarHeight = getToolbarHeight(context, isLandscape, isTablet)
//        val offset = statusBarHeight + toolbarHeight
        val offset = toolbarHeight
        val orientation = Resources.getSystem().configuration.orientation
        var navigationBarHeight = getNavigationBarHeight(context)
        val isNotchDisplay = isNotchDisplay(statusBarHeight)

        if (size.height % 10 == 0) {
            navigationBarHeight = 0
        }

        if (isTablet) {
            if (!this.isLandscape) {
                this.width = min(size.width, size.height) + if (orientation == Configuration.ORIENTATION_LANDSCAPE) navigationBarHeight else 0
                this.height = max(size.width, size.height) - offset + if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0 else navigationBarHeight
            } else {
                this.width = max(size.width, size.height) + if (orientation == Configuration.ORIENTATION_PORTRAIT) navigationBarHeight else 0
                this.height = min(size.width, size.height) - offset + if (orientation == Configuration.ORIENTATION_PORTRAIT) 0 else navigationBarHeight
            }
        } else {
            if (!this.isLandscape) {
                this.width = min(size.width, size.height)
                this.height = max(size.width, size.height) - offset + if (isNotchDisplay) statusBarHeight else 0
            } else {
                this.width = max(size.width, size.height) + if (isNotchDisplay) statusBarHeight else 0
                this.height = min(size.width, size.height) - offset
            }
        }

        Log.i("Ding:navigationBar", "" + navigationBarHeight)
        Log.i("Ding:statusBar", "" + statusBarHeight)
        Log.i("Ding:actionBar", "" + toolbarHeight)
        Log.i("Ding:before", "" + size.width + " * " + size.height)
        Log.i("Ding:offset", "" + offset)
    }

    public fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    public fun getToolbarHeight(context: Context, isLandscape: Boolean, isTablet: Boolean): Int {
        var actionBarHeight = 0f

        if (isTablet) {
            val tv = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics).toFloat()
            }
        } else {
            val scale = Resources.getSystem().displayMetrics.density
            actionBarHeight = if (!isLandscape) 56 * scale else 48 * scale
        }

        return actionBarHeight.toInt()
    }

    public fun getNavigationBarHeight(context: Context): Int {
        val resources: Resources = context.getResources()
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    public fun isNotchDisplay(statusBarHeight: Int): Boolean {
        return statusBarHeight > 24 * Resources.getSystem().displayMetrics.density
    }

    class FTDynamicTemplateCodableInfo(templateDynamicInfoDict: NSDictionary, scale: Float) {

        @SerializedName(CodingKeys.width)
        @Expose
        var width: Int = 0
        @SerializedName(CodingKeys.height)
        @Expose
        var height: Int = 0
        @SerializedName(CodingKeys.leftMargin)
        @Expose
        var leftMargin: Float = 99.toFloat()
        @SerializedName(CodingKeys.topMargin)
        @Expose
        var topMargin: Float = 65.toFloat()
        @SerializedName(CodingKeys.rightMargin)
        @Expose
        var rightMargin: Float = 663.toFloat()
        @SerializedName(CodingKeys.bottomMargin)
        @Expose
        var bottomMargin: Float = 44.toFloat()
        @SerializedName(CodingKeys.horizontalSpacing)
        @Expose
        var horizontalSpacing: Float = 33.toFloat()
        @SerializedName(CodingKeys.verticalSpacing)
        @Expose
        var verticalSpacing: Float = 4.toFloat()
        @SerializedName(CodingKeys.bgColor)
        @Expose
        var bgColor: String = "#FFFED6-1.0"
        @SerializedName(CodingKeys.horizontalLineColor)
        @Expose
        var horizontalLineColor: String = "#000000-0.15";
        @SerializedName(CodingKeys.verticalLineColor)
        @Expose
        var verticalLineColor: String = "#000000-0.15";
        @SerializedName(CodingKeys.themeClassName)
        @Expose
        var themeClassName: String = "FTPlainTemplateFormat"
        @SerializedName(CodingKeys.supportingDeviceType)
        @Expose
        var supportingDeviceType: Int = 0

        internal interface CodingKeys {
            companion object {
                const val width = "width"
                const val height = "height"
                const val leftMargin = "leftMargin"
                const val topMargin = "topMargin"
                const val rightMargin = "rightMargin"
                const val bottomMargin = "bottomMargin"
                const val horizontalSpacing = "horizontalSpacing"
                const val verticalSpacing = "verticalSpacing"
                const val bgColor = "bgColor"
                const val horizontalLineColor = "horizontalLineColor"
                const val verticalLineColor = "verticalLineColor"
                const val themeClassName = "themeClassName"
                const val supportingDeviceType = "supporting_device_type"
                const val dynamicTemplateInfo = "dynamic_template_info"
            }
        }

        init {

            if (templateDynamicInfoDict.containsKey(CodingKeys.dynamicTemplateInfo)) {
                var dynamicTemplateInfoDictionary = templateDynamicInfoDict.objectForKey(CodingKeys.dynamicTemplateInfo) as NSDictionary
                /*this.width                  = ((dynamicTemplateInfoDictionary[CodingKeys.width] as NSNumber).intValue() * scale).toInt()
                this.height                 = ((dynamicTemplateInfoDictionary[CodingKeys.height] as NSNumber).intValue() * scale).toInt()*/
                this.leftMargin             = (dynamicTemplateInfoDictionary[CodingKeys.leftMargin] as NSNumber).floatValue() * scale
                this.topMargin              = (dynamicTemplateInfoDictionary[CodingKeys.topMargin] as NSNumber).floatValue() * scale
                this.rightMargin            = (dynamicTemplateInfoDictionary[CodingKeys.rightMargin] as NSNumber).floatValue() * scale
                this.bottomMargin           = (dynamicTemplateInfoDictionary[CodingKeys.bottomMargin] as NSNumber).floatValue() * scale
                this.horizontalSpacing      = (dynamicTemplateInfoDictionary[CodingKeys.horizontalSpacing] as NSNumber).floatValue() * scale
                this.verticalSpacing        = (dynamicTemplateInfoDictionary[CodingKeys.verticalSpacing] as NSNumber).floatValue() * scale
                this.bgColor                = dynamicTemplateInfoDictionary[CodingKeys.bgColor].toString()
                this.horizontalLineColor    = dynamicTemplateInfoDictionary[CodingKeys.horizontalLineColor].toString()
                this.verticalLineColor      = dynamicTemplateInfoDictionary[CodingKeys.verticalLineColor].toString()
                this.themeClassName         = dynamicTemplateInfoDictionary[CodingKeys.themeClassName].toString()
                this.supportingDeviceType   = (dynamicTemplateInfoDictionary[CodingKeys.supportingDeviceType] as NSNumber).intValue()
                /*this.width = ((templateDynamicInfoDict[CodingKeys.width] as NSNumber).intValue() * scale).toInt()
                this.height = ((templateDynamicInfoDict[CodingKeys.height] as NSNumber).intValue() * scale).toInt()
                this.leftMargin = (templateDynamicInfoDict[CodingKeys.leftMargin] as NSNumber).floatValue() * scale
                this.topMargin = (templateDynamicInfoDict[CodingKeys.topMargin] as NSNumber).floatValue() * scale
                this.rightMargin = (templateDynamicInfoDict[CodingKeys.rightMargin] as NSNumber).floatValue() * scale
                this.bottomMargin = (templateDynamicInfoDict[CodingKeys.bottomMargin] as NSNumber).floatValue() * scale
                this.horizontalSpacing = (templateDynamicInfoDict[CodingKeys.horizontalSpacing] as NSNumber).floatValue() * scale
                this.verticalSpacing = (templateDynamicInfoDict[CodingKeys.verticalSpacing] as NSNumber).floatValue() * scale
                this.bgColor = templateDynamicInfoDict[CodingKeys.bgColor].toString()
                this.horizontalLineColor = templateDynamicInfoDict[CodingKeys.horizontalLineColor].toString()
                this.verticalLineColor = templateDynamicInfoDict[CodingKeys.verticalLineColor].toString()
                this.themeClassName = templateDynamicInfoDict[CodingKeys.themeClassName].toString()
                this.supportingDeviceType = (templateDynamicInfoDict[CodingKeys.supportingDeviceType] as NSNumber).intValue()*/
            }

            this.width                  = ((templateDynamicInfoDict[CodingKeys.width] as NSNumber).intValue() * scale).toInt()
            this.height                 = ((templateDynamicInfoDict[CodingKeys.height] as NSNumber).intValue() * scale).toInt()
        }

    }
}
