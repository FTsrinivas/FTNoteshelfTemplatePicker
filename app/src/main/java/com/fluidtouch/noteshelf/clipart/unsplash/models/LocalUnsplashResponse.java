package com.fluidtouch.noteshelf.clipart.unsplash.models;

import java.util.ArrayList;
import java.util.List;

public class LocalUnsplashResponse {
    private List<UnsplashPhotoInfo> clipartList = new ArrayList<>();

    public List<UnsplashPhotoInfo> getClipartList() {
        return clipartList;
    }

    public void setClipartList(List<UnsplashPhotoInfo> clipartList) {
        this.clipartList = clipartList;
    }
}