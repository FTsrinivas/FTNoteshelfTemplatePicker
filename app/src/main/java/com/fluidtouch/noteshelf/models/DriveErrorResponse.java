package com.fluidtouch.noteshelf.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sreenu on 13/02/19
 */
public class DriveErrorResponse {
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("errors")
    @Expose
    private List<DriveErrorErrorsResponse> errors = null;
    @SerializedName("message")
    @Expose
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<DriveErrorErrorsResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<DriveErrorErrorsResponse> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
