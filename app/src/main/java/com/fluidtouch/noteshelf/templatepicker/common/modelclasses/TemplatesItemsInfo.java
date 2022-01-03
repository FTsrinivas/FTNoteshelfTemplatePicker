package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

public class TemplatesItemsInfo {
    String templateCategoryName;
    String templateName;
    String templateImgID;

    public TemplatesItemsInfo(String templateCategoryName, String templateName, String templateImgID) {
        this.templateCategoryName = templateCategoryName;
        this.templateName = templateName;
        this.templateImgID = templateImgID;
    }

    public String getTemplateCategoryName() {
        return templateCategoryName;
    }

    public void setTemplateCategoryName(String templateCategoryName) {
        this.templateCategoryName = templateCategoryName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateImgID() {
        return templateImgID;
    }

    public void setTemplateImgID(String templateImgID) {
        this.templateImgID = templateImgID;
    }
}
