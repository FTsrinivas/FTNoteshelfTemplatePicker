package com.fluidtouch.noteshelf.generator.models.spacesInfo;

import com.google.gson.annotations.SerializedName;

public class AndroidDeviceInfo {
    @SerializedName("Port")
    public DeviceInfoData port;
    @SerializedName("Land")
    public DeviceInfoData land;
    @SerializedName("Mobile")
    public DeviceInfoData mobile;
}