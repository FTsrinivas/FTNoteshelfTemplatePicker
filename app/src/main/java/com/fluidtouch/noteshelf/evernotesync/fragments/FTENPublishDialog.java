package com.fluidtouch.noteshelf.evernotesync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.evernotesync.FTENPublishManager;
import com.fluidtouch.noteshelf.evernotesync.adapters.FTENErrorAdapter;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vineet on 02/05/2019
 */

public class FTENPublishDialog extends FTBaseDialog {
    @BindView(R.id.dialog_evernote_publish_last_sync_text_view)
    TextView enLastSyncTextView;
    @BindView(R.id.dialog_evernote_error_recycler_view)
    RecyclerView enErrorRecyclerView;
    @BindView(R.id.dialog_evernote_log_text_view)
    TextView logTextView;

    public static FTENPublishDialog newInstance() {
        return new FTENPublishDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_evernote_publish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        FTENErrorAdapter enErrorAdapter = new FTENErrorAdapter();
        enErrorRecyclerView.setAdapter(enErrorAdapter);

        //Displaying global level error and all notebook level errors
        List<String> errors = FTENPublishManager.getInstance().getErrorList();
        if (errors != null && !errors.isEmpty()) {
            logTextView.setVisibility(View.VISIBLE);
            enErrorAdapter.addAll(errors);
        } else {
            logTextView.setVisibility(View.GONE);
            enErrorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        enLastSyncTextView.setText(getString(R.string.evernote_last_sync_time, ""));
        String lastSync = FTApp.getPref().get(SystemPref.EVERNOTE_LAST_SYNC, "");
        if (lastSync != null && !lastSync.isEmpty()) {
            enLastSyncTextView.setText(getString(R.string.evernote_last_sync_time, lastSync));
        } else {
            enLastSyncTextView.setText(R.string.sync_has_not_started_yet);
        }
    }

    @OnClick(R.id.dialog_evernote_publish_notebooks)
    void onPublishNotebooksClicked() {
        FTENShelfItemDialog.newInstance(null).show(getChildFragmentManager());
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }
}