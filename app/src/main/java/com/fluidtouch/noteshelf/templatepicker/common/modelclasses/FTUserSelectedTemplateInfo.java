package com.fluidtouch.noteshelf.templatepicker.common.modelclasses;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;

public class FTUserSelectedTemplateInfo {

    private static FTUserSelectedTemplateInfo instance;
    FTTemplateColors ftTemplateColors;
    FTLineTypes ftLineTypes;
    FTNTheme ftnTheme;

    public static synchronized FTUserSelectedTemplateInfo getInstance() {
        if (instance == null) {
            instance = new FTUserSelectedTemplateInfo();
        }
        return instance;
    }

    public FTTemplateColors getFtTemplateColors() {
        return ftTemplateColors;
    }

    public void setFtTemplateColors(FTTemplateColors ftTemplateColors) {
        this.ftTemplateColors = ftTemplateColors;
    }

    public FTLineTypes getFtLineTypes() {
        return ftLineTypes;
    }

    public void setFtLineTypes(FTLineTypes ftLineTypes) {
        this.ftLineTypes = ftLineTypes;
    }

    public FTNTheme getFtnTheme() {
        return ftnTheme;
    }

    public void setFtnTheme(FTNTheme ftnTheme) {
        this.ftnTheme = ftnTheme;
    }

}
