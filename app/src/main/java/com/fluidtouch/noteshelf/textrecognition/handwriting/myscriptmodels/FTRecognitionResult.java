package com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FTRecognitionResult {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("bounding-box")
    @Expose
    private BoundingBox boundingBox;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("words")
    @Expose
    private List<Word> words = null;
    @SerializedName("chars")
    @Expose
    private List<Letter> chars = null;
    @SerializedName("id")
    @Expose
    private String id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public List<Letter> getChars() {
        return chars;
    }

    public void setChars(List<Letter> chars) {
        this.chars = chars;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
