package com.fluidtouch.noteshelf.audio.popup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.audio.adapter.FTAudioSessionsAdapter;
import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf2.R;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 07/03/19
 */
public class FTAudioSessionsDialog extends FTBaseDialog.Popup implements FTAudioSessionsAdapter.FTSessionsContainerToAdapterCallback {
    @BindView(R.id.audio_pop_up_recycler_view)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.audio_pop_up_no_audios_text_view)
    protected TextView mNoAudiosTextView;

    private FTAudioSessionsAdapter mSessionsAdapter;
    private FTAudioRecording mRecording;
    private SessionsContainerCallback mContainerCallback;

    private Observer mAudioObserver = new Observer() {

        @Override
        public void update(Observable o, Object arg) {
            FTAudioPlayerStatus status = (FTAudioPlayerStatus) arg;

            if (status.audioRecordingName.equals(mRecording.fileName) && mSessionsAdapter != null) {
                mSessionsAdapter.applyDataChanges(status.getCurrentSessionIndex());
            }
        }
    };

    public static FTAudioSessionsDialog newInstance(FTAudioRecording recording, SessionsContainerCallback containerCallback) {
        FTAudioSessionsDialog audioSessionsDialog = new FTAudioSessionsDialog();
        audioSessionsDialog.mRecording = recording;
        audioSessionsDialog.mContainerCallback = containerCallback;
        return audioSessionsDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.START);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_audio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        onResume();
        mSessionsAdapter = new FTAudioSessionsAdapter(this);
        mSessionsAdapter.addAll(mRecording.getAudioTracks());
        mRecyclerView.setAdapter(mSessionsAdapter);

        if (mSessionsAdapter.getAll().isEmpty()) {
            mNoAudiosTextView.setVisibility(View.VISIBLE);
        } else {
            mNoAudiosTextView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.audio_pop_up_back_image_view)
    void closePopUp() {
        dismiss();
    }

    @OnClick(R.id.audio_pop_up_new_button)
    void addNew() {
        FTAudioPlayer.getInstance().recordNewTrack(getContext(), mRecording, mContainerCallback.getPath());
        mContainerCallback.dismissDialog();
        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            FTAudioPlayer.getInstance().addObserver(getContext(), mAudioObserver);
        }
    }

    @Override
    public void onDestroy() {
        if (getContext() != null) {
            FTAudioPlayer.getInstance().removeObserver(getContext(), mAudioObserver);
        }
        super.onDestroy();
    }

    @Override
    public void play(int currentSession) {
        FTAudioPlayer.getInstance().play(getContext(), mRecording, mContainerCallback.getPath(), currentSession);
    }

    @Override
    public int getCurrentSession() {
        if (FTAudioPlayer.getInstance().getCurrentAudioName().equals(mRecording.fileName))
            return FTAudioPlayer.getInstance().getCurrentSession();
        else
            return -1;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        mContainerCallback.dismissDialog();
        super.onCancel(dialog);
    }

    public interface SessionsContainerCallback {
        void dismissDialog();

        String getPath();
    }
}
