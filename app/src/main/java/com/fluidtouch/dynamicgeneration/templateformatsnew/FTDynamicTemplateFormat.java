package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.res.Resources;
import android.util.Log;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.ColorRGB;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateMoreDetailsInfo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FTDynamicTemplateFormat {

    /*FTTemplateUtil ftTemplateUtil;
    FTTemplateColors ftTemplateColorsInfo;*/
    FTLineTypes ftLineTypesInfo;
    //FTSelectedDeviceInfo ftSelectedDeviceInfo;

    ColorRGB bgClrRGB;
    ColorRGB horizontalLinesClrRGB;
    ColorRGB verticalLinesClrRGB;

    Float mPageWidth  = 100f;
    Float mPageHeight = 100f;

    String cachePath        = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/";
    String tempPath         = FTConstants.TEMP_FOLDER_PATH;
    String tempPagesPath    = FTConstants.TEMP_FOLDER_PATH + "Pages/";
    String basePDFPath      = FTConstants.TEMP_FOLDER_PATH;
    float verticalSpacing   = 0.0f;
    float horizontalSpacing = 0.0f;

    FTNDynamicTemplateTheme mTheme;
    float scale = Resources.getSystem().getDisplayMetrics().density;

    int themeBgRedClrValue   = 255;
    int themeBgGreenClrValue = 255;
    int themeBgBlueClrValue  = 255;

    int horizontalLineRedClrValue   = 255;
    int horizontalLineGreenClrValue = 255;
    int horizontalLineBlueClrValue  = 255;


    int verticalLineRedClrValue   = 255;
    int verticalLineGreenClrValue = 255;
    int verticalLineBlueClrValue  = 255;

    public FTDynamicTemplateFormat(FTNDynamicTemplateTheme theme) {
        FTTemplateMoreDetailsInfo ftTemplateMoreDetailsInfo = new FTTemplateMoreDetailsInfo();

        mTheme                          = theme;

        FTSelectedDeviceInfo ftSelectedDeviceInfo = theme.selectedDeviceInfo();
        /*if (mTheme.width == 0.0f && mTheme.height == 0.0f) {
            mTheme.width  = ftSelectedDeviceInfo.getPageWidth();
            mTheme.height = ftSelectedDeviceInfo.getPageHeight();
        }*/

        File tempFile = new File(tempPath);
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }

        bgClrRGB                        = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.themeBgClr);
        horizontalLinesClrRGB           = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.horizontalLineColor);
        verticalLinesClrRGB             = ftTemplateMoreDetailsInfo.getRGBValue(mTheme.verticalLineColor);

        themeBgRedClrValue              = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.themeBgClr);
        themeBgGreenClrValue            = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.themeBgClr);
        themeBgBlueClrValue             = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.themeBgClr);

        horizontalLineRedClrValue       = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.horizontalLineColor);
        horizontalLineGreenClrValue     = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.horizontalLineColor);
        horizontalLineBlueClrValue      = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.horizontalLineColor);

        verticalLineRedClrValue         = ftTemplateMoreDetailsInfo.getRedClrValue(mTheme.verticalLineColor);
        verticalLineGreenClrValue       = ftTemplateMoreDetailsInfo.getGreenClrValue(mTheme.verticalLineColor);
        verticalLineBlueClrValue        = ftTemplateMoreDetailsInfo.getBlueClrValue(mTheme.verticalLineColor);

        Log.d("TemplatePicker==>","FTTemplateBgClrAndLinesInfoPopup"
                +" dynamicId::-"+mTheme.dynamicId
                +" isDefaultTheme::-"+mTheme.isDefaultTheme
                +" themeBgClr::-"+mTheme.themeBgClr
                +" ftSelectedDeviceInfo themeBgClr::-"+ftSelectedDeviceInfo.getThemeBgClrHexCode());


        Log.d("TemplatePicker==>","FTDynamicTemplateFormat Size getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight()+" themeBgClrName::-"+ftSelectedDeviceInfo.getThemeBgClrName());

        Log.d("TemplatePicker==>","FTDynamicTemplateFormat getThemeBgClrHexCode::-"+themeBgRedClrValue+" themeBgGreenClrValue::-"+themeBgGreenClrValue+" themeBgBlueClrValue::-"+themeBgBlueClrValue);

        horizontalSpacing               = (float) mTheme.horizontalSpacing * scale;
        verticalSpacing                 = (float) mTheme.verticalSpacing * scale;

        mPageWidth                      =  mTheme.width;
        mPageHeight                     =  mTheme.height;

        Log.d("::TemplatePickerV2:::","Device Size::- FTDynamicTemplateFormat generateTemplate::-" +
                " mTheme.width::-"+mTheme.width+
                " mTheme.height::-"+mTheme.height);

    }


    public TemplatesGeneratorInterface getClassInstance(String className) {
        try {
            Class<?> loadClass = Class.forName("com.fluidtouch.dynamicgeneration.templateformatsnew." + className);
            Log.d("TemplatePicker==>","loadClass getClassInstance::-"+loadClass.getCanonicalName());
            //TemplatesSavingInfo templatesSavingInfo = (TemplatesSavingInfo) loadClass.newInstance();
            //val cons: Constructor<*> = c.getConstructor(FTDynamicTemplateInfo::class.java)
            Constructor constructor = loadClass.getConstructor(FTNDynamicTemplateTheme.class);
            Log.d("TemplatePicker==>","constructor getClassInstance::-"+constructor.getName()+" themeClassName::-"+mTheme.themeClassName);
            TemplatesGeneratorInterface templatesSavingInfo = (TemplatesGeneratorInterface) constructor.newInstance(mTheme);
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
