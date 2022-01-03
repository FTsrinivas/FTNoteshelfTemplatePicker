package com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel;

import java.util.ArrayList;

public class FTDeviceMetaData {

    public ArrayList<FTDeviceDataInfo> getmFTDeviceDataInfo() {
        return mFTDeviceDataInfo;
    }

    public void setmFTDeviceDataInfo(ArrayList<FTDeviceDataInfo> mFTDeviceDataInfo) {
        this.mFTDeviceDataInfo = mFTDeviceDataInfo;
    }

    ArrayList<FTDeviceDataInfo> mFTDeviceDataInfo;
}
