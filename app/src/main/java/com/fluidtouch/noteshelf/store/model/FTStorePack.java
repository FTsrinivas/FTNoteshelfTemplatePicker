package com.fluidtouch.noteshelf.store.model;

import java.util.ArrayList;

public class FTStorePack {
    String title;
    ArrayList<FTStorePackItem> packs = new ArrayList<>();
    int sectiontype;
    int numOfItems = 1;
    String profilePic = "";
    boolean isValid = false;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<FTStorePackItem> getPacks() {
        return packs;
    }

    public void setPacks(ArrayList<FTStorePackItem> packs) {
        this.packs = packs;
    }

    public int getSectiontype() {
        return sectiontype;
    }

    public void setSectiontype(int sectiontype) {
        this.sectiontype = sectiontype;
    }

    public int getNumOfItems() {
        return numOfItems;
    }

    public void setNumOfItems(int numOfItems) {
        this.numOfItems = numOfItems;
    }
}
