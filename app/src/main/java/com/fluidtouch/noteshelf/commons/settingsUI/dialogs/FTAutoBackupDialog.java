package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTBackupOperations;
import com.fluidtouch.noteshelf.backup.database.FTSelectBackupLocationDialog;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCredentials;
import com.fluidtouch.noteshelf.commons.settingsUI.adapters.BackUpErrorAdapter;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 05/02/19
 */
public class FTAutoBackupDialog extends FTBaseDialog {
    @BindView(R.id.dialog_backup_backing_up_to_text_view)
    TextView mBackingUpToTextView;
    @BindView(R.id.dialog_backup_error_recycler_view)
    RecyclerView mErrorRecyclerView;
    @BindView(R.id.dialog_backup_image_view)
    ImageView mImageView;
    @BindView(R.id.tvLog)
    TextView tvLog;
    @BindView(R.id.backup_location_layout)
    RelativeLayout mBackupLocationLayout;
    @BindView(R.id.backup_location_text_view)
    TextView tvBackupLocation;
    @BindView(R.id.dialog_back_button)
    ImageView mBackButton;

    private SystemPref.BackUpType backingUpTo = SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()];
    private AutoBackupDialogListener mListener;
    private BroadcastReceiver mBackUpSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) setLastBackedUpTime();
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() == null)
            mListener = (AutoBackupDialogListener) context;
        else
            mListener = (AutoBackupDialogListener) getParentFragment();
        context.registerReceiver(mBackUpSuccessReceiver, new IntentFilter(getString(R.string.intent_backup_completed)));
    }

    public static FTAutoBackupDialog newInstance(SystemPref.BackUpType backingUpTo) {
        FTAutoBackupDialog backupDialog = new FTAutoBackupDialog();
        backupDialog.backingUpTo = backingUpTo;
        return backupDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_auto_backup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mBackButton.setImageResource(getParentFragment() == null ? R.drawable.close_dark : R.drawable.back_dark);

        if (backingUpTo.equals(SystemPref.BackUpType.GOOGLE_DRIVE)) {
            mBackingUpToTextView.setText(getString(R.string.backing_up_to_x, getString(R.string.google_drive)));
            mImageView.setImageResource(R.drawable.gdrive);
        } else if (backingUpTo.equals(SystemPref.BackUpType.DROPBOX)) {
            mBackingUpToTextView.setText(getString(R.string.backing_up_to_x, getString(R.string.dropbox)));
            mImageView.setImageResource(R.drawable.dropbox);
        } else if (backingUpTo.equals(SystemPref.BackUpType.ONE_DRIVE)) {
            mBackingUpToTextView.setText(getString(R.string.backing_up_to_x, getString(R.string.one_drive)));
            mImageView.setImageResource(R.drawable.universal_onedrive);
        } else if (backingUpTo.equals(SystemPref.BackUpType.WEBDAV)) {
            mBackingUpToTextView.setText(getString(R.string.backing_up_to_x, getString(R.string.webdav)));
            mImageView.setImageResource(R.drawable.universal_webdav_big);
        }

        mBackupLocationLayout.setVisibility(backingUpTo.equals(SystemPref.BackUpType.WEBDAV) ? View.VISIBLE : View.GONE);
        FTWebDavCredentials webDavCredentials = FTApp.getPref().getWebDavCredentials();
        if (webDavCredentials != null && !TextUtils.isEmpty(webDavCredentials.getBackupFolder())) {
            String[] folders = webDavCredentials.getBackupFolder().split("/");
            tvBackupLocation.setText(folders[folders.length - 1]);
        } else tvBackupLocation.setText("");

        BackUpErrorAdapter adapter = new BackUpErrorAdapter();
        if (FTBackupOperations.getInstance() != null) {
            List<? extends FTBackupItem> backupErrors = FTBackupOperations.getInstance().getErrorList();
            tvLog.setVisibility(backupErrors.isEmpty() ? View.GONE : View.VISIBLE);
            adapter.addAll(backupErrors);
        } else {
            tvLog.setVisibility(View.GONE);
        }
        mErrorRecyclerView.setAdapter(adapter);

        setLastBackedUpTime();

        if (backingUpTo.ordinal() != FTApp.getPref().getBackUpType() || (!TextUtils.isEmpty(getTag()) && getTag().contentEquals(FTSelectBackupLocationDialog.class.getName()))) {
            for (Fragment fragment : getParentFragmentManager().getFragments()) {
                if (!TextUtils.isEmpty(fragment.getTag()) && fragment.getTag().contentEquals(FTSelectBackupLocationDialog.class.getName()))
                    getParentFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
            if (FTApp.getServicePublishManager() != null) {
                FTApp.getServicePublishManager().stopPublishing();
                adapter.clear();
                tvLog.setVisibility(View.GONE);
            }
            FTApp.getPref().saveBackUpType(backingUpTo);
            ObservingService.getInstance().postNotification("backup_error", null);
            FTApp.mFTServicePublishManager = null;
            FTApp.setUpPublishManager();
            FTApp.getServicePublishManager().startPublishing();
        }
        mListener.updateBackupType();
    }

    @Override
    public void onDestroy() {
        try {
            if (getContext() != null && mBackUpSuccessReceiver != null) {
                getContext().unregisterReceiver(mBackUpSuccessReceiver);
                mBackUpSuccessReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void setLastBackedUpTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aaa", Locale.getDefault());
        long lastUpdatedAt = FTApp.getPref().getLastBackUpAt();
        mBackingUpToTextView.setText(lastUpdatedAt == 0 ? getString(R.string.backup_in_progress) : getString(R.string.last_back_up_at_time, simpleDateFormat.format(new Date(lastUpdatedAt))));
    }

    @OnClick(R.id.dialog_backup_change_layout)
    void changeBackUpType() {
        if (FTBackupOperations.getInstance() == null) {
            return;
        }
        FTBackupOperations.getInstance().deleteAll();
        if (mListener != null) {
            mListener.openChooseCloudDialog();
        }
        dismiss();
    }

    @OnClick(R.id.backup_location_layout)
    void onBackupLocationClicked() {
        new FTSelectBackupLocationDialog("").show(getParentFragmentManager());
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @Override
    public void dismiss() {
        if (mListener != null)
            mListener.updateBackupType();
        super.dismiss();
    }

    public interface AutoBackupDialogListener {
        void updateBackupType();

        void openChooseCloudDialog();
    }
}