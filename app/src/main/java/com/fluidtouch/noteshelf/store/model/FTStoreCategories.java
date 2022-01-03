package com.fluidtouch.noteshelf.store.model;

import java.util.ArrayList;

public class FTStoreCategories {
    ArrayList<FTStoreCategoryItem> categories = new ArrayList<>();

    public ArrayList<FTStoreCategoryItem> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<FTStoreCategoryItem> categories) {
        this.categories = categories;
    }
}
