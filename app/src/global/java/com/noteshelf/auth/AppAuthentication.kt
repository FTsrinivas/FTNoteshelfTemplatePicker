package com.noteshelf.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.fluidtouch.noteshelf.commons.FTLog
import com.fluidtouch.noteshelf2.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import java.util.*

class AppAuthentication(val context: Context) : AuthStateListener {
    private var mAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null
    var mListener: AuthChangeListener? = null

    fun initAuthentication() {
        mAuth = FirebaseAuth.getInstance()
        mAuth?.addAuthStateListener(this)
    }

    companion object {
        fun isLoginEnabled(context: Context?): Boolean {
            val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            when (resultCode) {
                ConnectionResult.SERVICE_MISSING or ConnectionResult.SERVICE_INVALID -> return false
            }
            return true
        }
    }

    fun setCallBackListener(listener: AuthChangeListener) {
        mListener = listener;
    }

    fun isUserSignedIn(): Boolean {
        return currentUser != null && currentUser?.isEmailVerified()!!
    }

    fun setCurrentUser() {
        currentUser = mAuth!!.currentUser
        if (currentUser != null && !currentUser?.isEmailVerified!!) {
            currentUser!!.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mListener?.onUserSignIn()
                } else {
                }
            }
        }
    }

    fun getUserName(): String? {
        return currentUser?.displayName
    }

    fun getUserEmail(): String? {
        return currentUser?.email
    }

    fun getUserPhotoUrl(): Uri? {
        return currentUser?.getPhotoUrl()
    }

    fun isUserEmailVarified(): Boolean {
        return currentUser?.isEmailVerified!!
    }

    fun getSignInIntent(): Intent {
        val providers = Arrays.asList(
                EmailBuilder().build(),
                GoogleBuilder().build(),
                FacebookBuilder().build())

        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.FirebaseUICustom)
                .setLogo(R.mipmap.nsclubpromo)
                .build()
    }

    fun signOut() {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener {
                    mListener?.onUserSignOut()
                    currentUser = null
                }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        currentUser = firebaseAuth.getCurrentUser()
        if (currentUser != null) {
            FTLog.logCrashCustomKey("Club Member", "Yes")
            if (currentUser!!.isEmailVerified) {
                mListener?.onUserSignIn()
            } else {
                currentUser!!.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mListener?.onUserSignIn()
                    }
                }
            }
        } else {
            FTLog.logCrashCustomKey("Club Member", "No")
        }
    }

    interface AuthChangeListener {
        fun onUserSignIn()
        fun onUserSignOut()
    }

}