package com.fluidtouch.noteshelf.document.enums;

import java.io.Serializable;

public enum FTToolBarTools implements Serializable {
    PEN, HIGHLIGHTER, ERASER, TEXT, VIEW, IMAGE, LASSO, AUDIO;

    public static FTToolBarTools initWithRawValue(int value) {
        FTToolBarTools type = PEN;
        switch (value) {
            case 0:
                type = PEN;
                break;
            case 1:
                type = HIGHLIGHTER;
                break;
            case 2:
                type = ERASER;
                break;
            case 3:
                type = TEXT;
                break;
            case 4:
                type = VIEW;
                break;
            case 5:
                type = IMAGE;
                break;
            case 6:
                type = LASSO;
                break;
            case 7:
                type = AUDIO;
                break;
            default:
                break;
        }
        return type;
    }

//    public static FTToolBarTools currentMode() {
//        PenRackPref preferences = new PenRackPref().init(PenRackPref.PREF_NAME);
//        int toolTypeInt = preferences.get(PenRackPref.PEN_TOOL, -1);
//        return FTToolBarTools.initWithRawValue(toolTypeInt);
//    }

    public int toInt() {
        switch (this) {
            case PEN:
                return 0;
            case HIGHLIGHTER:
                return 1;
            case ERASER:
                return 2;
            case TEXT:
                return 3;
            case VIEW:
                return 4;
            case IMAGE:
                return 5;
            case LASSO:
                return 6;
            case AUDIO:
                return 7;
        }
        return 0;
    }
}
