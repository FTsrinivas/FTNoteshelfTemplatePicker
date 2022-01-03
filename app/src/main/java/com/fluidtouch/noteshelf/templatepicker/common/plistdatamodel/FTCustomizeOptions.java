package com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel;

import java.util.ArrayList;

public class FTCustomizeOptions {
    boolean background_color;
    ArrayList<FTColorVariants> mFTColorVariants;
    ArrayList<FTLineTypes> mFTLineTypes;

    public boolean isBackground_color() {
        return background_color;
    }

    public void setBackground_color(boolean background_color) {
        this.background_color = background_color;
    }

    public ArrayList<FTColorVariants> getmFTColorVariants() {
        return mFTColorVariants;
    }

    public void setmFTColorVariants(ArrayList<FTColorVariants> mFTColorVariants) {
        this.mFTColorVariants = mFTColorVariants;
    }

    public ArrayList<FTLineTypes> getmFTLineTypes() {
        return mFTLineTypes;
    }

    public void setmFTLineTypes(ArrayList<FTLineTypes> mFTLineTypes) {
        this.mFTLineTypes = mFTLineTypes;
    }
}
