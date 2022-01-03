package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Stroke {

    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("X")
    @Expose
    private List<Double> x = null;
    @SerializedName("Y")
    @Expose
    private List<Double> y = null;
    @SerializedName("F")
    @Expose
    private List<Integer> f = null;
    @SerializedName("T")
    @Expose
    private List<Integer> t = null;
    @SerializedName("id")
    @Expose
    private String id;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getY() {
        return y;
    }

    public void setY(List<Double> y) {
        this.y = y;
    }

    public List<Integer> getF() {
        return f;
    }

    public void setF(List<Integer> f) {
        this.f = f;
    }

    public List<Integer> getT() {
        return t;
    }

    public void setT(List<Integer> t) {
        this.t = t;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
