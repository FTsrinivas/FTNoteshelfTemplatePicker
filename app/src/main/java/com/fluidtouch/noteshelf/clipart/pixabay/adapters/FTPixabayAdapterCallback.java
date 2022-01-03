package com.fluidtouch.noteshelf.clipart.pixabay.adapters;

import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;

public interface FTPixabayAdapterCallback {
    void onClipartSelected(Clipart clipart, boolean delete);

    void reorderCliparts(int from, int to);
}
