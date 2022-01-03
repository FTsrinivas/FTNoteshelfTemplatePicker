package com.fluidtouch.noteshelf.clipart.unsplash.providers;

import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;

import java.util.List;

public interface FTUnsplashProviderCallback {
    void onLoadCliparts(List<UnsplashPhotoInfo> clipartList, ClipartError clipartError);

    void onClipartDownloaded(String path);
}