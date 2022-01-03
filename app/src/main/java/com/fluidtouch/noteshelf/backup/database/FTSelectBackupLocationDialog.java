package com.fluidtouch.noteshelf.backup.database;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCloudHelper;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCredentials;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTAutoBackupDialog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSelectBackupLocationDialog extends FTBaseDialog implements FTBackupLocationAdapter.Listener {
    @BindView(R.id.backup_location_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.dialog_title)
    TextView mTitleTextView;
    @BindView(R.id.backup_location_backup_button)
    Button mBackupButton;

    private String mPreviousPath = "";

    public FTSelectBackupLocationDialog(String path) {
        mPreviousPath = path;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_backup_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        String title = mPreviousPath.contains("/") ? mPreviousPath.substring(mPreviousPath.lastIndexOf("/") + 1) : mPreviousPath;
        mTitleTextView.setText(TextUtils.isEmpty(mPreviousPath) ? getString(R.string.select_backup_location) : title);
        mBackupButton.setText(getString(R.string.backup_to_, TextUtils.isEmpty(mPreviousPath) ? "WebDAV" : title));

        FTBackupLocationAdapter adapter = new FTBackupLocationAdapter(this);
        mRecyclerView.setAdapter(adapter);

        FTWebDavCloudHelper webDavCloudHelper = new FTWebDavCloudHelper(getContext());

        final FTSmartDialog smartDialog = new FTSmartDialog();
        smartDialog.setMessage(getString(R.string.loading));
        smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
        smartDialog.show(getChildFragmentManager());

        webDavCloudHelper.getBackupFolders(mPreviousPath, response -> {
            if (isAdded()) {
                adapter.addAll((List<String>) response);
                smartDialog.dismiss();
            }
        });
    }

    @OnClick(R.id.backup_location_backup_button)
    void onBackupClicked() {
        FTWebDavCredentials webDavCredentials = FTApp.getPref().getWebDavCredentials();
        if (webDavCredentials != null) {
            if (!mPreviousPath.isEmpty() && !mPreviousPath.endsWith("/")) mPreviousPath += "/";

            webDavCredentials.setBackupFolder(mPreviousPath.isEmpty() ? getString(R.string.root) : mPreviousPath);
            ObservingService.getInstance().postNotification("pathChangeObserver", webDavCredentials);
            FTApp.getPref().saveWebDavCredentials(webDavCredentials);

            if (FTApp.getServicePublishManager() != null) {
                FTApp.mFTServicePublishManager.stopPublishing();
                FTApp.mFTServicePublishManager = null;
                FTApp.getPref().saveWebDavCredentials(webDavCredentials);
            }
            FTApp.getPref().saveBackUpType(SystemPref.BackUpType.WEBDAV);
            new FTWebDavBackupOperations().deleteAll();
            FTApp.mFTServicePublishManager = new FTServicePublishManager();
            FTApp.getServicePublishManager().startPublishing();
        }

        FragmentManager parentFragmentManager = getParentFragmentManager();
        List<Fragment> fragments = getParentFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (!TextUtils.isEmpty(fragment.getTag()) && fragment.getTag().contentEquals(getClass().getName()) && fragment != this) {
                parentFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
        }
        for (Fragment fragment : parentFragmentManager.getFragments()) {
            if (fragment instanceof FTAutoBackupDialog) {
                ((FTAutoBackupDialog) fragment).dismiss();
                break;
            }
        }
        FTAutoBackupDialog.newInstance(SystemPref.BackUpType.WEBDAV).show(parentFragmentManager);
        dismiss();
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        super.show(fragmentManager, getClass().getName());
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @Override
    public void onLocationSelected(String path) {
        new FTSelectBackupLocationDialog(mPreviousPath + (mPreviousPath.isEmpty() ? "" : "/") + path).show(getParentFragmentManager());
    }
}
