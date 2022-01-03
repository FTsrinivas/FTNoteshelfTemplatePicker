package com.fluidtouch.noteshelf.backup.database;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCredentials;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServiceAccountHandler;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWebDavCredentialsDialog extends FTBaseDialog {
    @BindView(R.id.webdav_creds_url_edit_text)
    EditText urlEditText;
    @BindView(R.id.webdav_creds_username_edit_text)
    EditText usernameEditText;
    @BindView(R.id.webdav_creds_password_edit_text)
    EditText passwordEditText;

    private final FTServiceAccountHandler mServiceAccountHandler;

    public FTWebDavCredentialsDialog(FTServiceAccountHandler serviceAccountHandler) {
        mServiceAccountHandler = serviceAccountHandler;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.3f);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.getDecorView().setElevation(0);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_webdav_credentials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.connect_button)
    void onConnectClicked() {
        if (TextUtils.isEmpty(urlEditText.getText()) || !FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())
                || !Patterns.WEB_URL.matcher(urlEditText.getText().toString().trim()).matches()) {
            sendAuthBroadcast(null);
            dismiss();
        } else {
            FTWebDavCredentials credentials = new FTWebDavCredentials();
            credentials.setUsername(usernameEditText.getText().toString().trim());
            credentials.setPassword(passwordEditText.getText().toString().trim());
            credentials.setServerAddress(urlEditText.getText().toString().trim());

            if (!credentials.getServerAddress().endsWith("/")) {
                credentials.setServerAddress(credentials.getServerAddress() + "/");
            }

            final FTSmartDialog smartDialog = new FTSmartDialog();
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
            smartDialog.setMessage(getString(R.string.connecting));
            smartDialog.show(getChildFragmentManager());
            ((FTWebDavServiceAccountHandler) mServiceAccountHandler).signIn(credentials, response -> {
                smartDialog.dismiss();
                FTApp.getPref().saveWebDavCredentials((response != null && (response.code() == 200 || response.code() == 207)) ? credentials : null);
                sendAuthBroadcast(response);
                onCancelClicked();
            });
        }
    }

    @OnClick(R.id.cancel_button)
    void onCancelClicked() {
        getParentFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    private void sendAuthBroadcast(okhttp3.Response response) {
        Context context = FTApp.getInstance().getApplicationContext();
        Intent intent = new Intent();
        intent.putExtra(context.getString(R.string.intent_is_successful), response != null && (response.code() == 200 || response.code() == 207));
        intent.putExtra("responseCode", response == null ? 0 : response.code());
        intent.setAction(context.getString(R.string.intent_sign_in_result));
        context.sendBroadcast(intent);
    }
}
