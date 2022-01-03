package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Letter {

    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("word")
    @Expose
    private Integer word;
    @SerializedName("grid")
    @Expose
    private List<Grid> grid = null;
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

    public Integer getWord() {
        return word;
    }

    public void setWord(Integer word) {
        this.word = word;
    }

    public List<Grid> getGrid() {
        return grid;
    }

    public void setGrid(List<Grid> grid) {
        this.grid = grid;
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
