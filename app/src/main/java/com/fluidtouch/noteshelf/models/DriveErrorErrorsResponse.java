package com.fluidtouch.noteshelf.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sreenu on 13/02/19
 */
public class DriveErrorErrorsResponse {
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("locationType")
    @Expose
    private String locationType;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("reason")
    @Expose
    private String reason;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
