package com.fluidtouch.noteshelf.audio.popup;

import android.app.Dialog;
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

import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.audio.adapter.FTAudioAdapter;
import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 13/03/19
 */
public class FTAudioDialog extends FTBaseDialog.Popup implements FTAudioAdapter.FTAudioContainerToAdapterListener, FTAudioSessionsDialog.SessionsContainerCallback {
    @BindView(R.id.audio_pop_up_recycler_view)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.audio_pop_up_no_audios_text_view)
    protected TextView mNoAudiosTextView;

    private FTAudioAdapter mAudioAdapter;
    private List<FTAudioAnnotation> mAudioAnnotations;
    private List<Integer> mPageNumbers;
    private String mRootPath;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("d MMM yy, H:mm aaa", Locale.getDefault());
    private AudioDialogContainerCallback mParentCallback;

    private Observer mAudioObserver = new Observer() {

        @Override
        public void update(Observable o, Object arg) {
            FTAudioPlayerStatus status = (FTAudioPlayerStatus) arg;

            if (mAudioAdapter != null && status.getPlayerMode() != FTAudioPlayerStatus.FTPlayerMode.PLAYING_STARTED) {
                mAudioAdapter.applyDataChanges(status);
            }
        }
    };

    public static FTAudioDialog newInstance(List<FTAudioAnnotation> audioRecordings, List<Integer> pageNumbers, AudioDialogContainerCallback parentCallback, FTUrl fileURL) {
        FTAudioDialog audioDialog = new FTAudioDialog();
        audioDialog.mAudioAnnotations = audioRecordings;
        audioDialog.mPageNumbers = pageNumbers;
        audioDialog.mRootPath = fileURL.getPath() + "/";
        audioDialog.mParentCallback = parentCallback;
        return audioDialog;
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
        mAudioAdapter = new FTAudioAdapter(mPageNumbers, this);
        mAudioAdapter.addAll(getAudioRecordings(mAudioAnnotations));
        mRecyclerView.setAdapter(mAudioAdapter);

        if (mAudioAdapter.getAll().isEmpty()) {
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
        mParentCallback.addNewAudio();
        dismiss();
    }

    @Override
    public void showSessions(FTAudioRecording audioRecording) {
        FTAudioSessionsDialog.newInstance(audioRecording, this).show(getChildFragmentManager());
    }

    @Override
    public void playAudio(FTAudioRecording audioRecording) {
        FTAudioPlayer.getInstance().play(getContext(), audioRecording, mRootPath);
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
    public String getCreatedTime(int position) {
        return mDateFormat.format(mAudioAnnotations.get(position).createdTimeInterval * 1000);
    }

    @Override
    public void dismissDialog() {
        dismiss();
    }

    @Override
    public String getPath() {
        return mRootPath;
    }


    public List<FTAudioRecording> getAudioRecordings(List<FTAudioAnnotation> annotations) {
        List<FTAudioRecording> audioRecordings = new ArrayList<>();
        for (int i = 0; i < annotations.size(); i++) {
            audioRecordings.add(((FTAudioAnnotationV1)annotations.get(i)).getAudioRecording());
        }

        return audioRecordings;
    }

    public interface AudioDialogContainerCallback {
        void addNewAudio();
    }
}
