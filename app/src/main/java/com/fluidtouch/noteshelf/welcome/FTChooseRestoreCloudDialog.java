package com.fluidtouch.noteshelf.welcome;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServiceAccountHandler;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTChooseRestoreCloudDialog extends FTBaseDialog {

    private final BroadcastReceiver mBackUpSignInResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(getString(R.string.intent_is_successful), false) && getParentFragment() != null) {
                ((RestoreDialogListener) getParentFragment()).onCloudChosen();
            } else {
                Toast.makeText(getContext(), "Failed to login. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context.registerReceiver(mBackUpSignInResultReceiver, new IntentFilter(getString(R.string.intent_sign_in_result)));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (isMobile()) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            bottomSheetDialog.getBehavior().setDraggable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            dialog.setCanceledOnTouchOutside(true);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_choose_restore_cloud, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (FTApp.isForHuawei()) {
            view.findViewById(R.id.tvGoogleDrive).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            if (FTServiceAccountHandler.getInstance() instanceof FTDropboxServiceAccountHandler) {
                Intent intent = new Intent();
                String mOAuth2Token = FTDropboxServiceAccountHandler.getOAuth2Token();
                intent.putExtra(getString(R.string.intent_is_successful), mOAuth2Token != null);
                if (mOAuth2Token != null) {
                    FTApp.getPref().saveDropBoxToken(mOAuth2Token);
                }
                mBackUpSignInResultReceiver.onReceive(getContext(), intent);
            } else if (FTServiceAccountHandler.getInstance() != null) {
                if (FTServiceAccountHandler.getInstance().checkSession(getContext())) {
                    if (getParentFragment() != null)
                        ((RestoreDialogListener) getParentFragment()).onCloudChosen();
                } else {
                    Toast.makeText(getContext(), "Failed to login. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null) getContext().unregisterReceiver(mBackUpSignInResultReceiver);
    }

    @OnClick({R.id.tvGoogleDrive, R.id.tvDropbox})
    void onCloudClicked(View view) {
        switch (view.getId()) {
            case R.id.tvGoogleDrive:
                FTFirebaseAnalytics.logEvent("shelf_activity", "restore", "google_drive");
                FTApp.getPref().saveBackUpType(SystemPref.BackUpType.GOOGLE_DRIVE);
                break;
            case R.id.tvDropbox:
                FTFirebaseAnalytics.logEvent("shelf_activity", "restore", "dropbox");
                FTApp.getPref().saveBackUpType(SystemPref.BackUpType.DROPBOX);
                break;
            default:
                FTApp.getPref().saveBackUpType(SystemPref.BackUpType.NONE);
                break;
        }
        FTServiceAccountHandler.getInstance().signIn(getContext());

    }

    public interface RestoreDialogListener {
        void onCloudChosen();
    }
}