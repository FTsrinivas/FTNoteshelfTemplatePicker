package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.res.Resources;
import android.util.Log;

import com.fluidtouch.dynamicgeneration.inteface.ContentStretchTemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNContentStretchTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.ColorRGB;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateMoreDetailsInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FTContentStretchTemplateFormat {

    ColorRGB bgClrRGB;
    FTNContentStretchTemplateTheme mTheme;

    Float mPageWidth  = 100f;
    Float mPageHeight = 100f;

    String basePDFPath      = FTConstants.TEMP_FOLDER_PATH;

    float scale = Resources.getSystem().getDisplayMetrics().density;
    float verticalSpacing   = 0.0f;
    float horizontalSpacing = 0.0f;

    int themeBgRedClrValue   = 255;
    int themeBgGreenClrValue = 255;
    int themeBgBlueClrValue  = 255;

    public FTContentStretchTemplateFormat(FTNContentStretchTemplateTheme theme) {
        FTTemplateMoreDetailsInfo ftTemplateMoreDetailsInfo = new FTTemplateMoreDetailsInfo();

        mTheme                          = theme;

        FTSelectedDeviceInfo ftSelectedDeviceInfo = theme.selectedDeviceInfo();
        if (mTheme.width == 0.0f && mTheme.height == 0.0f) {
            mTheme.width  = ftSelectedDeviceInfo.getPageWidth();
            mTheme.height = ftSelectedDeviceInfo.getPageHeight();
        }

        bgClrRGB                        = ftTemplateMoreDetailsInfo.getRGBValue(ftSelectedDeviceInfo.getThemeBgClrHexCode());

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat Size getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight()+" themeBgClrName::-"+mTheme.themeBgClr);

        themeBgRedClrValue              = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.themeBgClr);
        themeBgGreenClrValue            = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.themeBgClr);
        themeBgBlueClrValue             = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.themeBgClr);

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat getThemeBgClrHexCode::-"+themeBgRedClrValue+
                                            " themeBgGreenClrValue::-"+themeBgGreenClrValue+
                                            " themeBgBlueClrValue::-"+themeBgBlueClrValue);

        /*bgClrRGB                        = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.themeBgClr);
        horizontalLinesClrRGB           = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.horizontalLineColor);
        verticalLinesClrRGB             = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.verticalLineColor);

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat Size getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight()+" themeBgClrName::-"+mTheme.themeBgClrName);

        themeBgRedClrValue              = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.themeBgClr);
        themeBgGreenClrValue            = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.themeBgClr);
        themeBgBlueClrValue             = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.themeBgClr);

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat getThemeBgClrHexCode::-"+themeBgRedClrValue+" themeBgGreenClrValue::-"+themeBgGreenClrValue+" themeBgBlueClrValue::-"+themeBgBlueClrValue);

        horizontalLineRedClrValue       = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.horizontalLineColor);
        horizontalLineGreenClrValue     = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.horizontalLineColor);
        horizontalLineBlueClrValue      = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.horizontalLineColor);

        verticalLineRedClrValue         = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.verticalLineColor);
        verticalLineGreenClrValue       = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.verticalLineColor);
        verticalLineBlueClrValue        = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.verticalLineColor);*/

        horizontalSpacing               = (float) theme.horizontalSpacing * scale;
        verticalSpacing                 = (float) theme.verticalSpacing * scale;

        mPageWidth                      =  mTheme.width;
        mPageHeight                     =  mTheme.height;

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat Condition Line Selected " +
                " FTDynamicTemplateFormat bgColor::-" +theme.themeBgClr+
                " theme horizontalLineColor::-"+theme.horizontalLineColor+
                " theme verticalLineColor::-"+theme.verticalLineColor+
                " theme horizontalSpacing::-"+theme.horizontalSpacing+
                " theme verticalSpacing::-"+theme.verticalSpacing+
                " theme width::-"+theme.width+
                " theme height::-"+theme.height);

    }


    public ContentStretchTemplatesGeneratorInterface getClassInstance(String className) {
        try {
            Class<?> loadClass = Class.forName("com.fluidtouch.dynamicgeneration.templateformatsnew." + className);
            Log.d("TemplatePicker==>","loadClass getClassInstance::-"+loadClass.getCanonicalName());
            Constructor constructor = loadClass.getConstructor(FTNContentStretchTemplateTheme.class);
            Log.d("TemplatePicker==>","constructor getClassInstance::-"+constructor.getName()+" themeClassName::-"+mTheme.themeClassName);
            ContentStretchTemplatesGeneratorInterface templatesSavingInfo = (ContentStretchTemplatesGeneratorInterface) constructor.newInstance(mTheme);
            Log.d("TemplatePicker==>","templatesSavingInfo getClassInstance::-"+templatesSavingInfo);
            return templatesSavingInfo;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
