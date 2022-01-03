package com.fluidtouch.noteshelf.preferences;

import android.graphics.Color;

import com.fluidtouch.renderingengine.annotation.FTPenType;

/**
 * Created by Sreenu on 23/08/18
 */
public class PenRackPref extends FTBasePref {
    public static final String PREF_NAME = "PenRackPref";
    public static final String DEFAULT_PEN_COLOR = "#151515";
    public static final String DEFAULT_PEN_TYPE = FTPenType.pen.toString();
    public static final String DEFAULT_HIGHLIGHTER_COLOR = "#F8E81C";
    public static final String DEFAULT_HIGHLIGHTER_TYPE = FTPenType.highlighter.toString();
    public static final int DEFAULT_SIZE = 4;
    public static final String PEN_TOOL = "_pen_tool";
    public static final String PEN_TOOL_OLD = "_pen_tool_old";
    public static final String SELECTED_ERASER_SIZE = "selectedEraserSize";
    public static final String ERASER_OPTIONS = "mPrefEraserOptions";
    public static final String CHECK_BOX_KEY = "mPrefCheckBoxKey";
    public static final String CURRENT_SELECTION = "mPrefCurrentSelection";
    public static final String AUTO_SELECTION_PREVIOUS_TOOL_SWITCH = "mPrefAutoSelectPrevToolSwitch";
    public static final String ERASE_ENTIRE_STROKE_SWITCH = "mPrefEraseEntireStrokeSwitch";
    public static final String ERASE_HIGHLIGHTER_STROKE_SWITCH = "mPrefEraseHighlighterStrokeSwitch";

    @Override
    public PenRackPref init(String prefName) {
        setSharedPreferences(prefName);
        return this;
    }

    public Integer getSelectedPenColor() {
        return get(PrefKeys.SELECTED_PEN_COLOR, Color.parseColor(DEFAULT_PEN_COLOR));
    }

    public void saveSelectedPenColor(int selectedPenColor) {
        save(PrefKeys.SELECTED_PEN_COLOR, selectedPenColor);
    }

    private final class PrefKeys {
        private static final String SELECTED_PEN_COLOR = "selectedPenColor";
    }

    public String getEraserOptions() {
        return get(ERASER_OPTIONS, "on");
    }

    public void saveEraserOptions(String value) {
        save(ERASER_OPTIONS, value);
    }
}
