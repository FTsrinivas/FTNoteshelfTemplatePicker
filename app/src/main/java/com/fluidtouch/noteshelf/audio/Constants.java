package com.fluidtouch.noteshelf.audio;

import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.FTApp;

/**
 * Created by Sreenu on 06/03/19
 */
public class Constants {
    public static final String AUDIO_ROOT_PATH = ContextCompat.getDataDir(FTApp.getInstance().getCurActCtx()).getPath() + "/audio/";
    public static final String AUDIO_ANNOTATIONS_ROOT_PATH = ContextCompat.getDataDir(FTApp.getInstance().getCurActCtx()).getPath() + "/annotations/";
}
