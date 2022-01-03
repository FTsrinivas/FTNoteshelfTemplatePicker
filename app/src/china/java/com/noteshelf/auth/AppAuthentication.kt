package com.noteshelf.auth

import android.content.Context
import android.content.Intent
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.noteshelf.auth.ui.HMSLoginActivity

class AppAuthentication(val context: Context) {
    var agConnectAuth: AGConnectAuth? = null
    var currentUser: AGConnectUser? = null
    var mListener: AuthChangeListener? = null

    fun initAuthentication() {
        agConnectAuth = AGConnectAuth.getInstance();
    }

    companion object {
        fun isLoginEnabled(context: Context?): Boolean {
            return true
        }
    }

    fun setCallBackListener(listener: AuthChangeListener) {
        mListener = listener;
    }

    fun isUserSignedIn(): Boolean {
        return currentUser != null
    }

    fun setCurrentUser() {
        currentUser = agConnectAuth!!.currentUser
    }

    fun getUserName(): String? {
        return currentUser?.displayName
    }

    fun getUserEmail(): String? {
        if (currentUser?.email == null)
            return getUserName()
        return currentUser?.email
    }

    fun getUserPhotoUrl(): String? {
        return currentUser?.getPhotoUrl()
    }

    fun isUserEmailVarified(): Boolean {
        return true
    }

    fun getSignInIntent(): Intent {
        return Intent(context, HMSLoginActivity::class.java)
    }

    fun signOut() {
        agConnectAuth?.signOut()
        mListener?.onUserSignIn()
        currentUser = null
    }

    interface AuthChangeListener {
        fun onUserSignIn()
        fun onUserSignOut()
    }

}