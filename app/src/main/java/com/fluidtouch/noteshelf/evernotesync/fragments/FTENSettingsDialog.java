package com.fluidtouch.noteshelf.evernotesync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.evernotesync.FTENPublishManager;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTENSettingsDialog extends FTBaseDialog {
    @BindView(R.id.backup_settings_dialog_title)
    TextView titleTextView;
    @BindView(R.id.backup_settings_dialog_progress)
    ProgressBar enProgressBar;
    @BindView(R.id.backup_settings_dialog_data_used_text_view)
    TextView enProgressTextView;
    @BindView(R.id.backup_settings_dialog_username)
    TextView enUsernameTextView;

    public static FTENSettingsDialog newInstance() {
        return new FTENSettingsDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_backup_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        titleTextView.setText(R.string.evernote);

        FTENPublishManager.getInstance().getUserName(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (null != getContext()) {
                        enUsernameTextView.setText(FTApp.getPref().get(SystemPref.EVERNOTE_USERNAME, ""));
                        float uploadLimit = FTApp.getPref().get(SystemPref.EVERNOTE_USER_UPLOAD_LIMIT, 0F);
                        float uploadedSize = FTApp.getPref().get(SystemPref.EVERNOTE_USER_UPLOADED_SIZE, 0F);
                        int dataUserInPercent = Math.round((uploadedSize / uploadLimit) * 100);
                        enProgressBar.setProgress(dataUserInPercent);
                        enProgressTextView.setText(getString(R.string.evernote_progress_text, uploadedSize, uploadLimit));
                    }
                });
            }
        });
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick(R.id.backup_settings_dialog_sign_out_button)
    void onSignOutClicked() {
        FTENPublishManager.getInstance().disablePublisher();
        EvernoteSession.getInstance().logOut();
        if (getParentFragment() != null)
            ((ENSettingsDialogListener) getParentFragment()).onEvernoteSignOut();
        dismiss();
    }

    public interface ENSettingsDialogListener {
        void onEvernoteSignOut();
    }
}