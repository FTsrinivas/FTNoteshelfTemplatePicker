package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

public class TemplatesLineInfo {

    int horizontalLineSpacing;
    String lineType;
    int verticalLineSpacing;
    int lineTypeIcon;

    public TemplatesLineInfo (int horizontalLineSpacing, String lineType,
                              int verticalLineSpacing,int lineTypeIcon) {
        this.horizontalLineSpacing = horizontalLineSpacing;
        this.lineType = lineType;
        this.verticalLineSpacing = verticalLineSpacing;
        this.lineTypeIcon = lineTypeIcon;
    }

    public int getLineTypeIcon() {
        return lineTypeIcon;
    }

    public void setLineTypeIcon(int lineTypeIcon) {
        this.lineTypeIcon = lineTypeIcon;
    }

    public int getHorizontalLineSpacing() {
        return horizontalLineSpacing;
    }

    public void setHorizontalLineSpacing(int horizontalLineSpacing) {
        this.horizontalLineSpacing = horizontalLineSpacing;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public int getVerticalLineSpacing() {
        return verticalLineSpacing;
    }

    public void setVerticalLineSpacing(int verticalLineSpacing) {
        this.verticalLineSpacing = verticalLineSpacing;
    }

}
