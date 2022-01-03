package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BoundingBox {

    @SerializedName("x")
    @Expose
    private Double x;
    @SerializedName("y")
    @Expose
    private Double y;
    @SerializedName("width")
    @Expose
    private Double width;
    @SerializedName("height")
    @Expose
    private Double height;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

}
