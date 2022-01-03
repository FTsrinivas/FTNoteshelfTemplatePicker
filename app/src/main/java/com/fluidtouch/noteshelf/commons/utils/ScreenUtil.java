package com.fluidtouch.noteshelf.commons.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SizeF;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;


public class ScreenUtil {

    private ScreenUtil() {
        throw new IllegalStateException("NumberUtils class");
    }

    public static int getScreenWidth(Context context) {
        Log.d("TemplatePicker==>","FTDynamicTemplateFormat sizes getScreenWidth::-"+context.getResources().getDisplayMetrics().widthPixels);
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        Log.d("TemplatePicker==>","FTDynamicTemplateFormat sizes getScreenHeight::-"+context.getResources().getDisplayMetrics().heightPixels);
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * @param context Current screen context.
     * @param dp      Value
     * @return Converted pixel value.
     */
    public static int convertDpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int convertPxToDp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    private void reference(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
    }

    public static boolean isTablet() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        return diagonalInches >= 6.5;
    }

    public static boolean isMobile(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) <= Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    //Not working
    public static boolean isNavigationBarVisible(Context context) {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            return true;
        }
        return false;
    }

    public static Point getDisplaySize(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static SizeF setScreenSize(Context context) {
        int x, y, orientation = context.getResources().getConfiguration().orientation;
        WindowManager wm = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);
        x = screenSize.x;
        y = screenSize.y;
        int width = getWidth(x, y, orientation);
        int height = getHeight(x, y, orientation);
        return new SizeF(width, height);
    }

    private static int getWidth(int x, int y, int orientation) {
        return orientation == Configuration.ORIENTATION_PORTRAIT ? x : y;
    }

    private static int getHeight(int x, int y, int orientation) {
        return orientation == Configuration.ORIENTATION_PORTRAIT ? y : x;
    }

    public static boolean hasNavBar(Context context, View view) {
        if (view == null)
            return false;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int height = view.getHeight() + location[1];
        return (setScreenSize(context).getHeight() - height) > 10;
    }
    public static String ellipsize(String input, int maxLength) {
        String ellip = "...";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxLength - ellip.length()).concat(ellip);
    }
}
