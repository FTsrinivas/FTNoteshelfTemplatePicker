package com.fluidtouch.noteshelf.templatepicker.common;

import java.util.ArrayList;

public class FTTemplateCategoryInfo {

    String categoryName;
    ArrayList<String> paperName;

    public FTTemplateCategoryInfo(String categoryName, ArrayList<String> paperName) {
        this.categoryName = categoryName;
        this.paperName = paperName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ArrayList<String> getPaperName() {
        return paperName;
    }

    public void setPaperName(ArrayList<String> paperName) {
        this.paperName = paperName;
    }

}
