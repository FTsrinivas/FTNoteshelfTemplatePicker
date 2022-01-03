package com.fluidtouch.noteshelf.templatepicker.interfaces;

import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;

public interface TemplateBackgroundListener {
    interface TemplateInfoRequest {
        public void tempLineAndColorInfoResponse(FTTemplateColors colorVariantsInfo,
                                                 FTLineTypes lineTypeInfo,
                                                 FTSelectedDeviceInfo ftSelectedDeviceInfo);
        public void moreColorViewSelected();

        public void templateBgColourChangedListener();
    }

    interface CallBackToShowClub {
        public void showClub();
    }

}
