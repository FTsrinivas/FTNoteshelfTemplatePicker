package com.fluidtouch.noteshelf.store.model;

import java.util.ArrayList;

public class FTStoreMetadata {
    ArrayList<FTStorePack> sections = new ArrayList<>();

    public ArrayList<FTStorePack> getSections() {
        return sections;
    }

    public void setSections(ArrayList<FTStorePack> sections) {
        this.sections = sections;
    }
}
