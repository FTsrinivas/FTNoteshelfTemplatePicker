package com.fluidtouch.noteshelf.clipart.unsplash.adapters;

import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;

public interface FTUnsplashAdapterCallback {
    void onClipartSelected(UnsplashPhotoInfo clipart, boolean delete);

    void reorderCliparts(int from, int to);

    void onPhotographerNameSelected(int position);
}
