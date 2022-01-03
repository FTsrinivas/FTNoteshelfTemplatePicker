package com.fluidtouch.noteshelf.commons.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FTPermissionManager {
    public static boolean checkPermission(Context mContext, Activity activity, String[] permissions, int REQUEST_CODE) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(mContext, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
                } else {
                    return true;
                }
            } else {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}
