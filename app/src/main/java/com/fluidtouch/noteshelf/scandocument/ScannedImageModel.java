package com.fluidtouch.noteshelf.scandocument;

import android.graphics.PointF;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sreenu on 22/05/19
 */
public class ScannedImageModel {
    public String originalImagePath;
    public String croppedImagePath;
    public Map<Integer, PointF> croppingPoints = new HashMap<>();
}
