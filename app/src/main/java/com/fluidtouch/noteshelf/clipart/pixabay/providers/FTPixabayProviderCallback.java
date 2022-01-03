package com.fluidtouch.noteshelf.clipart.pixabay.providers;

import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;

import java.util.List;

public interface FTPixabayProviderCallback {
    void onLoadCliparts(List<Clipart> clipartList, ClipartError clipartError);

    void onClipartDownloaded(String path);
}
