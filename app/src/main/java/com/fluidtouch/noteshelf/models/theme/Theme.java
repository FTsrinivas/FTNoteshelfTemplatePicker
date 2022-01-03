package com.fluidtouch.noteshelf.models.theme;

import android.content.Context;

import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.List;

public class Theme {
    private String toolbarColor;
    private int drawableResId;

    public static List<Theme> getThemes(Context context) {
        List<Theme> themes = new ArrayList<>();
        String[] themesArray = context.getResources().getStringArray(R.array.theme_drawable_array);
        for (String s : themesArray) {
            String[] eachTheme = s.split(",");
            if (eachTheme.length == 2) {
                Theme theme = new Theme();
                theme.drawableResId = context.getResources().getIdentifier(eachTheme[0], "drawable", context.getPackageName());
                theme.toolbarColor = eachTheme[1];
                themes.add(theme);
            }
        }
        return themes;
    }

    public String getToolbarColor() {
        return toolbarColor;
    }

    public int getDrawableResId() {
        return drawableResId;
    }
}