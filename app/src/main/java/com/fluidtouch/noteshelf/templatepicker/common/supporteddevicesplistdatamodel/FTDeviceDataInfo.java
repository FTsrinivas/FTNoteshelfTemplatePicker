package com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel;

import java.util.ArrayList;

public class FTDeviceDataInfo {

    String deviceType;
    ArrayList<FTDevicesDetailedInfo> ftSupportedDevicesDetailedInfoArrayList;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public ArrayList<FTDevicesDetailedInfo> getFtSupportedDevicesDetailedInfoArrayList() {
        return ftSupportedDevicesDetailedInfoArrayList;
    }

    public void setFtSupportedDevicesDetailedInfoArrayList(ArrayList<FTDevicesDetailedInfo> ftSupportedDevicesDetailedInfoArrayList) {
        this.ftSupportedDevicesDetailedInfoArrayList = ftSupportedDevicesDetailedInfoArrayList;
    }

}
