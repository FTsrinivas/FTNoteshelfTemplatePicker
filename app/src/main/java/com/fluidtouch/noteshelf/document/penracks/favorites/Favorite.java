package com.fluidtouch.noteshelf.document.penracks.favorites;

import com.fluidtouch.renderingengine.annotation.FTPenType;

public class Favorite {
    private FTPenType penType;
    private String penColor;
    private int penSize;

    public Favorite(String penColor, FTPenType penType, int penSize) {
        this.penColor = penColor;
        this.penType = penType;
        this.penSize = penSize;
    }

    public FTPenType getPenType() {
        return penType;
    }

    public void setPenType(FTPenType penType) {
        this.penType = penType;
    }

    public String getPenColor() {
        return penColor;
    }

    public void setPenColor(String penColor) {
        this.penColor = penColor;
    }

    public int getPenSize() {
        return penSize;
    }

    public void setPenSize(int penSize) {
        this.penSize = penSize;
    }
}