package com.noteshelf.cloud

import android.content.Context

object FTCloudServices {

    fun isGoogleDriveWorking(): Boolean {
        return true
    }

    fun isDropBoxWorking(): Boolean {
        return true
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
        return true
    }

    fun isOneDriveWorking(): Boolean {
        return true;
    }
}