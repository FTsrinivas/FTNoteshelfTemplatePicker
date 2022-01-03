package com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel;

import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.google.gson.Gson;

public class FTSelectedDeviceInfo {

    int pageWidth  = 595;
    int pageHeight = 842;

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public void setSelectedDeviceName(String selectedDeviceName) {
        this.selectedDeviceName = selectedDeviceName;
    }

    public String getItemModel() {
        return itemModel;
    }

    public void setItemModel(ItemModel itemModel) {
        final Gson gson = new Gson();
        String serializedObject = null;
        try {
            serializedObject = gson.toJson(itemModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.itemModel = serializedObject;
    }

    String itemModel;

    String selectedDeviceName = "A4 8.3 x 11.7\"\"";

    String layoutType;

    String lineType;

    String themeBgClrName;
    String themeBgClrHexCode;

    String verticalLineClr;
    String horizontalLineClr;

    String themeMoreBgClrName;
    String themeMoreBgClrHexCode;

    String verticaMorelLineClr;
    String horizontalMoreLineClr;

    public int getVerticalLineSpacing() {
        return verticalLineSpacing;
    }

    public void setVerticalLineSpacing(int verticalLineSpacing) {
        this.verticalLineSpacing = verticalLineSpacing;
    }

    public int getHorizontalLineSpacing() {
        return horizontalLineSpacing;
    }

    public void setHorizontalLineSpacing(int horizontalLineSpacing) {
        this.horizontalLineSpacing = horizontalLineSpacing;
    }

    int verticalLineSpacing = 34;
    int horizontalLineSpacing = 34;

    public String getThemeMoreBgClrName() {
        return themeMoreBgClrName;
    }

    public void setThemeMoreBgClrName(String themeMoreBgClrName) {
        this.themeMoreBgClrName = themeMoreBgClrName;
    }

    public String getThemeMoreBgClrHexCode() {
        return themeMoreBgClrHexCode;
    }

    public void setThemeMoreBgClrHexCode(String themeMoreBgClrHexCode) {
        this.themeMoreBgClrHexCode = themeMoreBgClrHexCode;
    }

    public String getVerticaMorelLineClr() {
        return verticaMorelLineClr;
    }

    public void setVerticaMorelLineClr(String verticaMorelLineClr) {
        this.verticaMorelLineClr = verticaMorelLineClr;
    }

    public String getHorizontalMoreLineClr() {
        return horizontalMoreLineClr;
    }

    public void setHorizontalMoreLineClr(String horizontalMoreLineClr) {
        this.horizontalMoreLineClr = horizontalMoreLineClr;
    }

    public String getVerticalLineClr() {
        return verticalLineClr;
    }

    public void setVerticalLineClr(String verticalLineClr) {
        this.verticalLineClr = verticalLineClr;
    }

    public String getHorizontalLineClr() {
        return horizontalLineClr;
    }

    public void setHorizontalLineClr(String horizontalLineClr) {
        this.horizontalLineClr = horizontalLineClr;
    }

    public String getThemeBgClrHexCode() {
        return themeBgClrHexCode;
    }

    public void setThemeBgClrHexCode(String themeBgClrHexCode) {
        this.themeBgClrHexCode = themeBgClrHexCode;
    }

    public String getThemeBgClrName() {
        return themeBgClrName;
    }

    public void setThemeBgClrName(String themeBgClrName) {
        this.themeBgClrName = themeBgClrName;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public static FTSelectedDeviceInfo selectedDeviceInfo() {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();
        ftSelectedDeviceInfo.setLineType(FTApp.getPref().get(SystemPref.LINE_TYPE_SELECTED, "default"));
        ftSelectedDeviceInfo.setLayoutType(FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait"));

        ftSelectedDeviceInfo.setPageWidth(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_WIDTH, 595));
        ftSelectedDeviceInfo.setPageHeight(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_HEIGHT, 842));

        ftSelectedDeviceInfo.setThemeBgClrName(FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME, "ivory"));
        ftSelectedDeviceInfo.setThemeBgClrHexCode(FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX, "#F7F7F2-1.0"));
        ftSelectedDeviceInfo.setHorizontalLineClr(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX, "#000000-0.15"));
        ftSelectedDeviceInfo.setVerticalLineClr(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX, "#000000-0.15"));
        ftSelectedDeviceInfo.setHorizontalLineSpacing(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_HORIZONTAL_LINE_SPACING, 34));
        ftSelectedDeviceInfo.setVerticalLineSpacing(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_VERTICAL_LINE_SPACING, 34));

        ftSelectedDeviceInfo.setThemeMoreBgClrName(FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME_MRE, "Legal"));
        ftSelectedDeviceInfo.setThemeMoreBgClrHexCode(FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX_MRE, "#FFFED6-1.0"));
        ftSelectedDeviceInfo.setHorizontalMoreLineClr(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX_MRE, "#8FBECC-1.0"));
        ftSelectedDeviceInfo.setVerticaMorelLineClr(FTApp.getPref().get(SystemPref.TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX_MRE, "#C4A393-1.0"));

        FTApp.getPref().save(SystemPref.TEMPLATE_DEVICE_NAME, "A4 8.3 x 11.7\"\"");
        FTApp.getPref().save(SystemPref.TEMPLATE_MODEL_INFO, "ModelInfo");
        Log.d("TemplatePickerV2", "selectedDeviceInfo Getter Mani getLayoutType "
                + FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "mani")
                +" TEMPLATE_COLOUR_SELECTED_CLR_NAME:: "+FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME, "mani"));

        return ftSelectedDeviceInfo;
    }

    public void selectSavedDeviceInfo() {
        FTApp.getPref().save(SystemPref.LINE_TYPE_SELECTED, getLineType());
        FTApp.getPref().save(SystemPref.LAST_SELECTED_TAB, getLayoutType());

        FTApp.getPref().save(SystemPref.SELECTED_DEVICE_WIDTH, getPageWidth());
        FTApp.getPref().save(SystemPref.SELECTED_DEVICE_HEIGHT, getPageHeight());

        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME, getThemeBgClrName());
        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX, getThemeBgClrHexCode());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX, getHorizontalLineClr());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX, getVerticalLineClr());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_HORIZONTAL_LINE_SPACING, getHorizontalLineSpacing());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_VERTICAL_LINE_SPACING, getVerticalLineSpacing());

        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME_MRE, getThemeMoreBgClrName());
        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX_MRE, getThemeMoreBgClrHexCode());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX_MRE, getHorizontalMoreLineClr());
        FTApp.getPref().save(SystemPref.TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX_MRE, getVerticaMorelLineClr());

        FTApp.getPref().save(SystemPref.TEMPLATE_DEVICE_NAME, getSelectedDeviceName());
        FTApp.getPref().save(SystemPref.TEMPLATE_MODEL_INFO, getItemModel());

        Log.d("TemplatePickerV2", "selectSavedDeviceInfo Setter Mani getLayoutType " + getLayoutType()
                +" TEMPLATE_COLOUR_SELECTED_CLR_NAME  "+getThemeBgClrName());

    }

}
