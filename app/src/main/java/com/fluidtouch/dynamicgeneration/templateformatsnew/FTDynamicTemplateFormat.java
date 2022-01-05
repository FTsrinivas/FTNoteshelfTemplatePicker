package com.fluidtouch.dynamicgeneration.templateformatsnew;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.util.SizeF;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.FTScreenUtils;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.preferences.SystemPref;
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
    float scale/* = Resources.getSystem().getDisplayMetrics().density*/;
    int orientation = FTApp.getInstance().getResources().getConfiguration().orientation;

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
        float pageHeight    =   0.0f;
        float pageWeight    =   0.0f;
        mTheme                          = theme;

        FTSelectedDeviceInfo ftSelectedDeviceInfo = theme.selectedDeviceInfo();
        /*if (mTheme.width == 0.0f && mTheme.height == 0.0f) {
            mTheme.width  = ftSelectedDeviceInfo.getPageWidth();
            mTheme.height = ftSelectedDeviceInfo.getPageHeight();
        }*/
        SizeF thSize = new SizeF(theme.selectedDeviceInfo().getPageWidth(), theme.selectedDeviceInfo().getPageHeight());
        SizeF aspectSize = FTScreenUtils.INSTANCE.aspectSize(thSize, new SizeF(800f, 1200f));
        if (theme.isLandscape) {
            aspectSize = FTScreenUtils.INSTANCE.aspectSize(thSize, new SizeF(1200f, 800f));
        }
        scale = thSize.getWidth() / aspectSize.getWidth();

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

        //------------------------------------------------------------------------------------------------------------------------------------------------------------
        boolean isTablet = ScreenUtil.isTablet();
        int statusBarHeight = FTScreenUtils.INSTANCE.getStatusBarHeight(FTApp.getInstance());
        int offset =  FTScreenUtils.INSTANCE.getToolbarHeight(FTApp.getInstance(), mTheme.isLandscape, isTablet);
        int navigationBarHeight =  FTScreenUtils.INSTANCE.getNavigationBarHeight(FTApp.getInstance());
        boolean isNotchDisplay =  FTScreenUtils.INSTANCE.isNotchDisplay(statusBarHeight);

        if (mTheme.height % 10 == 0) {
            navigationBarHeight = 0;
        }
        if (FTApp.getPref().get(SystemPref.LAST_SELECTED_PAPER, "A4 8.3 x 11.7\"\"").equalsIgnoreCase("This Device")) {

            if (ScreenUtil.isTablet()) {
                if (!mTheme.isLandscape) {
                    pageWeight= min(mTheme.width, mTheme.height) + (orientation == Configuration.ORIENTATION_LANDSCAPE? navigationBarHeight : 0);
                    pageHeight  = max(mTheme.width, mTheme.height) - offset +  (orientation == Configuration.ORIENTATION_LANDSCAPE?0 : navigationBarHeight);
                } else {
                    pageWeight = max(mTheme.width, mTheme.height) +  (orientation == Configuration.ORIENTATION_PORTRAIT?navigationBarHeight : 0) ;
                    pageHeight  = min(mTheme.width, mTheme.height) - offset +  (orientation == Configuration.ORIENTATION_PORTRAIT?0 : navigationBarHeight);
                }
                Log.d("FTDynamicTemplateFormat==>","FTDynamicTemplateFormat width::-"+ mTheme.width+"  height::-"+ mTheme.height+"\n PageWidth::"+pageWeight +"\t pageHeight::"+pageHeight +"\n orientation:: "+orientation+"\t navBarHeight::"+offset);


               /* if (!mTheme.isLandscape) {
                    pageWeight         = min(mTheme.width, mTheme.height); *//*+ (orientation == Configuration.ORIENTATION_LANDSCAPE ? navigationBarHeight : 0);*//*
                    pageHeight         = max(mTheme.width, mTheme.height) - offset + (orientation == Configuration.ORIENTATION_LANDSCAPE ? 0 : navigationBarHeight);
                } else {
                    pageWeight         = max(mTheme.width, mTheme.height) *//*+ (orientation == Configuration.ORIENTATION_PORTRAIT ? navigationBarHeight : 0)*//*;
                    pageHeight         = min(mTheme.width, mTheme.height) - offset; *//* + (orientation == Configuration.ORIENTATION_PORTRAIT ? 0 : navigationBarHeight)*//*;
                }*/
            } else {
                if (!mTheme.isLandscape) {
                    pageWeight        = min(mTheme.width, mTheme.height);
                    pageHeight        = max(mTheme.width, mTheme.height) - offset + (isNotchDisplay ? statusBarHeight : 0);
                } else {
                    pageWeight        = max(mTheme.width, mTheme.height) + (isNotchDisplay ? statusBarHeight : 0);
                    pageHeight        = min(mTheme.width, mTheme.height) - offset;
                }
            }
            mPageWidth = pageWeight;
            mPageHeight = pageHeight;
            Log.d("::TemplatePickerV2:::","Device Size::- FTDynamicTemplateFormat generateTemplate::THIS DEVICE-" +
                    " mPageWidth::-"+mPageWidth+
                    " mPageHeight::-"+mPageHeight);
        } else {
            mPageWidth = mTheme.width;
            mPageHeight = mTheme.height;
            Log.d("::TemplatePickerV2:::","Device Size::- FTDynamicTemplateFormat generateTemplate::NO - THIS DEVICE-" +
                    " mPageWidth::-"+mPageWidth+
                    " mPageHeight::-"+mPageHeight);
        }

       /* mPageWidth                      =  mTheme.width;
        mPageHeight                     =  mTheme.height;*/



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
