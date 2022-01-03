package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Word {

    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("candidates")
    @Expose
    private List<String> candidates = null;
    @SerializedName("first-char")
    @Expose
    private Integer firstChar;
    @SerializedName("last-char")
    @Expose
    private Integer lastChar;
    @SerializedName("bounding-box")
    @Expose
    private BoundingBox boundingBox;
    @SerializedName("range")
    @Expose
    private String range;
    @SerializedName("strokes")
    @Expose
    private List<Stroke> strokes = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }

    public Integer getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(Integer firstChar) {
        this.firstChar = firstChar;
    }

    public Integer getLastChar() {
        return lastChar;
    }

    public void setLastChar(Integer lastChar) {
        this.lastChar = lastChar;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public List<Stroke> getStrokes() {
        return strokes;
    }

    public void setStrokes(List<Stroke> strokes) {
        this.strokes = strokes;
    }

}
