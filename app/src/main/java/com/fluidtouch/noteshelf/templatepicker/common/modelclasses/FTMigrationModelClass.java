package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

public class FTMigrationModelClass {

    String bgColor;
    String landscape_pad;
    String new_pack_name;
    String portrait_pad;
    int dynamic_id;
    String landscape_phone;

    public String getLandscape_phone() {
        return landscape_phone;
    }

    public void setLandscape_phone(String landscape_phone) {
        this.landscape_phone = landscape_phone;
    }

    public String getPortrait_phone() {
        return portrait_phone;
    }

    public void setPortrait_phone(String portrait_phone) {
        this.portrait_phone = portrait_phone;
    }

    String portrait_phone;


    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getLandscape_pad() {
        return landscape_pad;
    }

    public void setLandscape_pad(String landscape_pad) {
        this.landscape_pad = landscape_pad;
    }

    public String getNew_pack_name() {
        return new_pack_name;
    }

    public void setNew_pack_name(String new_pack_name) {
        this.new_pack_name = new_pack_name;
    }

    public String getPortrait_pad() {
        return portrait_pad;
    }

    public void setPortrait_pad(String portrait_pad) {
        this.portrait_pad = portrait_pad;
    }

    public int getDynamic_id() {
        return dynamic_id;
    }

    public void setDynamic_id(int dynamic_id) {
        this.dynamic_id = dynamic_id;
    }
}
