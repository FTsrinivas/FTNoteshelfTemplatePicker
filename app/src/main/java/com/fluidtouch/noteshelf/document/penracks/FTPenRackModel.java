package com.fluidtouch.noteshelf.document.penracks;

import android.content.Context;

import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.models.penrack.FTNPenRack;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTPenType;

import java.util.HashMap;

/**
 * Created by sreenu on 10/6/21.
 */
public class FTPenRackModel {
    public HashMap<String, Object> defaultRack;
    public int[] sizes;
    public Object[] fontSizes;
    public String rackName;
    public String prefPenKey = "";
    public String prefSizeKey = "";
    public String prefColorKey = "";
    public int defaultSize = 3;

    public static FTPenRackModel getDefaultPenRack(FTToolBarTools tool, Context context) {
        HashMap<String, Object> defaultRackData = FTNPenRack.getInstance().getPenRackData();
        FTPenRackModel model = new FTPenRackModel();
        if (tool == FTToolBarTools.PEN) {
            model.prefPenKey = "selectedPen";
            model.prefSizeKey = "selectedPenSize";
            model.prefColorKey = "selectedPenColor";
            model.defaultSize = PenRackPref.DEFAULT_SIZE;

            model.rackName = "FTDefaultPenRack";
            model.defaultRack = (HashMap<String, Object>) defaultRackData.get(model.rackName);
            model.sizes = context.getResources().getIntArray(R.array.pen_sizes);
            model.fontSizes = (Object[]) model.defaultRack.get("sizes");
        } else if (tool == FTToolBarTools.HIGHLIGHTER) {
            model.prefPenKey = "selectedPen_h";
            model.prefSizeKey = "selectedPenSize_h";
            model.prefColorKey = "selectedPenColor_h";
            model.defaultSize = PenRackPref.DEFAULT_SIZE;

            model.rackName = "FTDefaultHighlighterRack";
            model.defaultRack = (HashMap<String, Object>) defaultRackData.get(model.rackName);
            model.sizes = context.getResources().getIntArray(R.array.highlighter_sizes);
            model.fontSizes = (Object[]) model.defaultRack.get("sizes");
        } else {
            model.prefSizeKey = PenRackPref.SELECTED_ERASER_SIZE;
            model.defaultSize = PenRackPref.DEFAULT_SIZE - 1;

            model.rackName = "FTDefaultHighlighterRack";
            model.defaultRack = (HashMap<String, Object>) defaultRackData.get(model.rackName);
            model.sizes = context.getResources().getIntArray(R.array.eraser_sizes);
            model.fontSizes = new Object[]{1, 2, 3, 4};
        }

        return model;
    }

    public HashMap<String, Object> getUpdatedRack() {
        return (HashMap<String, Object>) FTNPenRack.getInstance().getDefaultRack(rackName);
    }

    public FTToolBarTools getPenRackGroupType(FTPenType penType) {
        if (penType == FTPenType.pen || penType == FTPenType.pilotPen || penType == FTPenType.caligraphy) {
            return FTToolBarTools.PEN;
        } else if (penType == FTPenType.highlighter || penType == FTPenType.flatHighlighter) {
            return FTToolBarTools.HIGHLIGHTER;
        }
        return FTToolBarTools.PEN;
    }
}
