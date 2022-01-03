package com.fluidtouch.noteshelf.document.search;

import android.graphics.RectF;

import java.io.Serializable;

public class FTSearchableItem implements Serializable {
    private RectF boundingRect = new RectF();
    private String text;
    private int rotation = 1;

    public RectF getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(RectF boundingRect) {
        this.boundingRect = boundingRect;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotated(int rotated) {
        rotation = rotated;
    }
}