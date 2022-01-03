package com.noteshelf.cloud

import android.content.Context

object FTCloudServices {

    fun isGoogleDriveWorking(): Boolean {
        return false
    }

    fun isDropBoxWorking(): Boolean {
        return false
    }

    fun isHuaweiDriveWorking(): Boolean {
        return false
    }

    fun isRestoreEnabled(): Boolean {
        return isGoogleDriveWorking() || isDropBoxWorking() || isHuaweiDriveWorking() || isOneDriveWorking()
    }

    fun isEverNoteWorking(): Boolean {
        return true
    }

    fun isGooglePlayServicesAvailable(context: Context?): Boolean {
        return false
    }

    fun isOneDriveWorking(): Boolean {
        return false;
    }
}