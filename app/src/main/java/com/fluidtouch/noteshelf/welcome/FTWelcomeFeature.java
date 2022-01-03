package com.fluidtouch.noteshelf.welcome;

public class FTWelcomeFeature {
    private int resourceId;
    private String title;

    public FTWelcomeFeature(int resourceId, String title) {
        this.resourceId = resourceId;
        this.title = title;
    }


    public int getResourceId() {
        return resourceId;
    }

    public String getTitle() {
        return title;
    }
}