package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class FTPopupFactory {
    public static PopupWindow create(Context context, View atView, int layout, int width, int height) {
        PopupWindow popupWindow = new PopupWindow(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(layout, null);

        popupWindow.setWidth(width != 0 ? context.getResources().getDimensionPixelOffset(width) : LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(height != 0 ? context.getResources().getDimensionPixelOffset(height) : LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(popUpView);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.showAsDropDown(atView, 0, 0);

        return popupWindow;
    }

    public static PopupWindow create(Context context, View atView, int layout, int width, int height, int xOffset) {
        PopupWindow popupWindow = new PopupWindow(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(layout, null);

        popupWindow.setWidth(width != 0 ? context.getResources().getDimensionPixelOffset(width) : LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(height != 0 ? context.getResources().getDimensionPixelOffset(height) : LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(popUpView);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.showAsDropDown(atView, xOffset, 0);

        return popupWindow;
    }
}