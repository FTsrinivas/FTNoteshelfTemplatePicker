package org.benjinus.pdfium;

import android.graphics.RectF;

public class Link {
    public void setBounds(RectF bounds) {
        this.bounds = bounds;
    }

    private RectF bounds;
    private Integer destPageIdx;
    private String uri;
    private Integer pageRotation;

    public Link(RectF bounds, Integer destPageIdx, String uri) {
        this.bounds = bounds;
        this.destPageIdx = destPageIdx;
        this.uri = uri;
    }

    private Link() {
    }

    public Integer getDestPageIdx() {
        return destPageIdx;
    }

    public String getUri() {
        return uri;
    }

    public RectF getBounds() {
        return bounds;
    }

    public Integer getPageRotation() {
        return pageRotation;
    }

    public void setPageRotation(Integer pageRotation) {
        this.pageRotation = pageRotation;
    }
}