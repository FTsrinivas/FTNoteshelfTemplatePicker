package com.fluidtouch.noteshelf.evernotesync;

import android.util.Log;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;

/**
 * Created by Vineet on 29/04/2019
 */

public class FTENSyncError {
    private String errorCode = "UNKNOWN";
    private String errorDescription = "Unknown";

    public FTENSyncError(Exception e) {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            this.errorCode = "AUTH_EXPIRED";
        } else if (e instanceof TException) {
            TException exception = (TException) e;
            this.errorDescription = exception.getMessage();
        } else if (e instanceof EDAMUserException) {
            EDAMUserException exception = (EDAMUserException) e;
            this.errorCode = exception.getErrorCode().name();
            this.errorDescription = exception.getParameter();
        } else if (e instanceof EDAMSystemException) {
            EDAMSystemException exception = (EDAMSystemException) e;
            this.errorCode = exception.getErrorCode().name();
        }
        Log.e(FTLog.NS_EVERNOTE, "Error Code: " + errorCode + "\tError Description " + errorDescription);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        setErrorDescription("");
        if (errorCode != null) {
            switch (this.errorCode) {
                //Ignore completely
                case "INTERNAL_ERROR":
                    break;
                case "SHARD_UNAVAILABLE":
                    break;
                case "RATE_LIMIT_REACHED":
                    break;
                //Notebook level errors
                case "DATA_CONFLICT":
                    break;
                case "ENML_VALIDATION":
                    break;
                case "DATA_REQUIRED":
                    break;
                case "LIMIT_REACHED":
                    break;
                case "LEN_TOO_SHORT":
                    break;
                case "LEN_TOO_LONG":
                    break;
                case "BAD_DATA_FORMAT":
                    break;
                case "TOO_FEW":
                    break;
                case "TOO_MANY":
                    break;
                case "TAKEN_DOWN":
                    break;
                case "UNSUPPORTED_OPERATION":
                    break;
                //Global level errors
                case "QUOTA_REACHED":
                    setErrorDescription("Quota reached!");
                    break;
                case "INVALID_AUTH":
                    setErrorDescription("Invalid Authentication. Please login again.");
                    break;
                case "PERMISSION_DENIED":
                    setErrorDescription("Permission Denied! Visit evernote account for account for permissions.");
                    break;
                case "AUTH_EXPIRED":
                    setErrorDescription("Authentication Expired. Please login again.");
                    break;
                case "UNKNOWN":
                    if (!FTApp.getInstance().isNetworkAvailable()) {
                        //ToDo: Send user for no internet connection
                        setErrorDescription("Internet not available.");
                    } else if (!EvernoteSession.getInstance().isLoggedIn()) {
                        //ToDo: Request user to login again
                        setErrorDescription("Please login again.");
                    }
                    break;
                default:
                    setErrorDescription("Unknown");
                    break;
            }
            return errorDescription;
        } else {
            return "Unknown";
        }
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}