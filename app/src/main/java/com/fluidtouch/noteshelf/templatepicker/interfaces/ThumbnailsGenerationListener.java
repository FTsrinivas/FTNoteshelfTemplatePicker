package com.fluidtouch.noteshelf.templatepicker.interfaces;

import android.content.Context;
import android.graphics.Bitmap;

import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;

public interface ThumbnailsGenerationListener {
    void thumbnailsGeneration(boolean status, Context mContext, FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder,
                              FTNPaperTheme ftnPaperTheme, Bitmap bitmap);
}
