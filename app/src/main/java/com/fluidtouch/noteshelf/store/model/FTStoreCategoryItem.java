package com.fluidtouch.noteshelf.store.model;

import java.util.ArrayList;

public class FTStoreCategoryItem {
    String category_name;
    ArrayList<String> themes = new ArrayList<>();

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public ArrayList<String> getThemes() {
        return themes;
    }

    public void setThemes(ArrayList<String> themes) {
        this.themes = themes;
    }
}
