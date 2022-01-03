package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Grid {

    @SerializedName("x")
    @Expose
    private Double x;
    @SerializedName("y")
    @Expose
    private Double y;

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

}
