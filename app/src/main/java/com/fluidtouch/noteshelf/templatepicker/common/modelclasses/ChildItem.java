package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

public class ChildItem {

    // Declaration of the variable
    private String colorName;
    private String colorHex;
    private String horizontalLineColor;
    private String verticalLineColor;

    // Constructor of the class
    // to initialize the variable*
    public ChildItem(String colorName, String colorHex,
                     String horizontalLineColor,String verticalLineColor) {
        this.colorName = colorName;
        this.colorHex = colorHex;
        this.horizontalLineColor = horizontalLineColor;
        this.verticalLineColor = verticalLineColor;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getHorizontalLineColor() {
        return horizontalLineColor;
    }

    public void setHorizontalLineColor(String horizontalLineColor) {
        this.horizontalLineColor = horizontalLineColor;
    }

    public String getVerticalLineColor() {
        return verticalLineColor;
    }

    public void setVerticalLineColor(String verticalLineColor) {
        this.verticalLineColor = verticalLineColor;
    }
}
