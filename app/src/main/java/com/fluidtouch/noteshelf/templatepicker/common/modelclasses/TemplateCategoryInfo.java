package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

import java.util.List;

public class TemplateCategoryInfo {

    String templateCategoryName;
    List<TemplatesItemsInfo> mTemplateCategoryInfo;

    public List<TemplatesItemsInfo> getmTemplateCategoryInfo() {
        return mTemplateCategoryInfo;
    }

    public void setmTemplateCategoryInfo(List<TemplatesItemsInfo> mTemplateCategoryInfo) {
        this.mTemplateCategoryInfo = mTemplateCategoryInfo;
    }

    public TemplateCategoryInfo(String templateCategoryName, List<TemplatesItemsInfo> mTemplateCategoryInfo) {
        this.templateCategoryName = templateCategoryName;
        this.mTemplateCategoryInfo =mTemplateCategoryInfo;
    }

    public String getTemplateCategoryName() {
        return templateCategoryName;
    }

    public void setTemplateCategoryName(String templateCategoryName) {
        this.templateCategoryName = templateCategoryName;
    }

}
