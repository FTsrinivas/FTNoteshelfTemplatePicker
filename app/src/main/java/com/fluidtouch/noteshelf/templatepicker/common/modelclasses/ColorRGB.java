package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

public class ColorRGB {

    int redClrValue;
    int greenClrValue;
    int blueClrValue;
    int bgcolorAlpha;

    public ColorRGB(int redClrValue, int greenClrValue, int blueClrValue,int bgcolorAlpha) {
        this.redClrValue = redClrValue;
        this.greenClrValue = greenClrValue;
        this.blueClrValue = blueClrValue;
        this.bgcolorAlpha = bgcolorAlpha;
    }

    public int getBgcolorAlpha() {
        return bgcolorAlpha;
    }

    public void setBgcolorAlpha(int bgcolorAlpha) {
        this.bgcolorAlpha = bgcolorAlpha;
    }

    public int getRedClrValue() {
        return redClrValue;
    }

    public void setRedClrValue(int redClrValue) {
        this.redClrValue = redClrValue;
    }

    public int getGreenClrValue() {
        return greenClrValue;
    }

    public void setGreenClrValue(int greenClrValue) {
        this.greenClrValue = greenClrValue;
    }

    public int getBlueClrValue() {
        return blueClrValue;
    }

    public void setBlueClrValue(int blueClrValue) {
        this.blueClrValue = blueClrValue;
    }


}
