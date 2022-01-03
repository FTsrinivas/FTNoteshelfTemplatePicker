package com.fluidtouch.noteshelf.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf2.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fluidtouch.noteshelf.audio.player.FTAudioPlayer.PLAYBACK_COMPLETION;

/**
 * Created by Sreenu on 05/03/19
 */
public class FTAudioToolbarFragment extends Fragment {
    //region View Bindings
    @BindView(R.id.audio_player_speed_text_view)
    protected TextView mSpeedTextView;
    @BindView(R.id.audio_player_time_text_view)
    protected TextView mTimeTextView;
    @BindView(R.id.audio_player_rec_image_view)
    protected ImageView mRecImageView;
    @BindView(R.id.audio_player_play_image_view)
    protected ImageView mPlayImageView;
    @BindView(R.id.audio_player_animation_image_view)
    protected ImageView mAnimationImageView;
    @BindView(R.id.audio_player_seek_bar)
    protected AppCompatSeekBar mSeekBar;
    @BindView(R.id.audio_player_animation_container)
    protected RelativeLayout mAnimationContainer;
    @BindView(R.id.audio_player_collapse_layout)
    protected LinearLayout mCollapseLayout;
    @BindView(R.id.audio_player_main_layout)
    LinearLayout mainLayout;
    //endregion

    //region Member Variables
    private float mSpeed = 1;
    private boolean isPlaying = false;
    private boolean isRecording = false;

    private FTAudioPlayer mAudioPlayer;
    private BroadcastReceiver mPlayBackCompletionReceiver;
    private FTAudioPlayerStatus.FTPlayerMode mCurrentMode;
    private boolean isForRecording = false;

    private PlayerFragmentContainerCallbacks mParentCallbacks;
    //endregion

    //region Lifecycle Methods

    private Observer mAudioObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            FTAudioPlayerStatus status = (FTAudioPlayerStatus) arg;
            Log.i("Player Status", status.getPlayerMode().toString());
            switch (status.getPlayerMode()) {
                case RECORDING_STARTED:
                    onRecordingStarted();
                    break;
                case RECORDING_PROGRESS:
                    setTime(mAudioPlayer.getRecordingTime());
                    break;
                case RECORDING_STOPPED:
                    onRecordingStopped();
                    break;
                case PLAYING_STARTED:
                    onPlayerStarted();
                    break;
                case PLAYING_PROGRESS:
                    onPlayerProgress();
                    break;
                case PLAYING_PAUSED:
                    onPlayerPaused();
                    break;
                case PLAYING_STOPPED:
                    onPlayerStopped();
                    break;
                case IDLE:
                    break;
            }
        }
    };

    public static FTAudioToolbarFragment newInstance(boolean isForRecording, PlayerFragmentContainerCallbacks callbacks) {
        FTAudioToolbarFragment fragment = new FTAudioToolbarFragment();
        fragment.isForRecording = isForRecording;
        fragment.mParentCallbacks = callbacks;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAudioPlayer = FTAudioPlayer.getInstance();
        FTAudioPlayer.getInstance().requestAudioFocus(getContext());

        setTime(0);
        setSpeed();

        mPlayBackCompletionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("path").equals(mAudioPlayer.getCurrentAudioName())) {
                    mSeekBar.setProgress(mSeekBar.getMax());
                    onPlayerStopped();
                }
            }
        };

        if (!isForRecording && null != mAudioPlayer && mAudioPlayer.mRecording.getTrackCount() > 0) {
            onPlayPauseTapped();
            mCurrentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS;
        } else {
            onRecordTapped();
            mCurrentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            getContext().registerReceiver(mPlayBackCompletionReceiver, new IntentFilter(PLAYBACK_COMPLETION));
            FTAudioPlayer.getInstance().addObserver(getContext(), mAudioObserver);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (getContext() != null) {
                FTAudioPlayer.getInstance().removeObserver(getContext(), mAudioObserver);
            }

            if (getContext() != null) {
                getContext().unregisterReceiver(mPlayBackCompletionReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
    //endregion

    //region On click events
    @OnClick(R.id.audio_player_rec_image_view)
    void onRecordTapped() {
        if (isPlaying) {
            mAudioPlayer.stopPlaying(getContext(), true);
        } else if (isRecording) {
            mAudioPlayer.stopRecording(getContext(), true);
        } else {
            mAudioPlayer.startRecording(getContext());
            onRecordingStarted();
        }
    }

    @OnClick(R.id.audio_player_play_image_view)
    void onPlayPauseTapped() {
        if (isRecording) {
            return;
        } else if (isPlaying) {
            mAudioPlayer.pausePlaying(getContext(), true);
        } else {
            mAudioPlayer.startPlaying(getContext(), mAudioPlayer.getCurrentSession(), mAudioPlayer.getCurrentProgress());
            onPlayerStarted();
        }
    }

    @OnClick(R.id.audio_payer_close_image_view)
    public void closeAudioToolbar() {
        FTAudioPlayer.getInstance().stopPlaying(getContext(), true);
        FTAudioPlayer.getInstance().stopRecording(getContext(), true);
        mParentCallbacks.onPlayerClosed();
        closeAudioToolbarFragment();
    }

    public void closeAudioToolbarFragment() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @OnClick(R.id.audio_player_speed_text_view)
    void changeSpeed() {
        if (FTAudioPlayer.getInstance().currentMode == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS) {
            return;
        }
        if (mSpeed >= 2.0) {
            mSpeed = 0.5f;
        } else {
            mSpeed += 0.5;
        }

        setSpeed();
    }

    @OnClick(R.id.audio_player_collapse_icon)
    void onToolbarCollapseClicked() {

    }
    //endregion

    //region Helper Methods

    //region start/stop Recording
    private void onRecordingStarted() {
        isRecording = true;
        setTime(mAudioPlayer.getRecordingTime());

        mRecImageView.setImageResource(R.drawable.audio_stop_dark);
        mPlayImageView.setImageResource(R.drawable.audio_play_light);
        mAnimationContainer.setVisibility(View.VISIBLE);
        mSeekBar.setVisibility(View.GONE);
        mTimeTextView.setTextColor(Color.parseColor("#d0021b"));

        ViewTreeObserver viewTreeObserver = mAnimationContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAnimationContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                TranslateAnimation animation = new TranslateAnimation(0.0f, mAnimationContainer.getMeasuredWidth() - mAnimationImageView.getMeasuredWidth(), 0.0f, 0.0f);
                animation.setDuration(2000);
                animation.setRepeatCount(Animation.INFINITE);
                animation.setRepeatMode(2);
                animation.setFillAfter(true);
                mAnimationImageView.startAnimation(animation);
            }
        });
    }

    private void onRecordingStopped() {
        mRecImageView.setImageResource(R.drawable.audio_rec);
        mPlayImageView.setImageResource(R.drawable.audio_play_dark);
        mAnimationContainer.setVisibility(View.GONE);
        mSeekBar.setVisibility(View.VISIBLE);
        mTimeTextView.setTextColor(Color.parseColor("#000000"));
        setTime(0);
        mSeekBar.setProgress(0);
        isRecording = false;
    }
    //endregion

    //region start/Pause/Stop playing

    private void onPlayerStarted() {
        mSeekBar.setMax((int) mAudioPlayer.getTotalDuration());
        mPlayImageView.setImageResource(R.drawable.audio_pause_dark);
        mRecImageView.setImageResource(R.drawable.audio_stop_dark);
        initializeSeekBar();
        isPlaying = true;
    }

    private void onPlayerProgress() {
        mPlayImageView.setImageResource(R.drawable.audio_pause_dark);
        mRecImageView.setImageResource(R.drawable.audio_stop_dark);
        int progress = mAudioPlayer.getCurrentProgress();
        mSeekBar.setProgress(progress);
        setTime(progress);
        isPlaying = true;
    }

    private void onPlayerPaused() {
        mPlayImageView.setImageResource(R.drawable.audio_play_dark);
        mRecImageView.setImageResource(R.drawable.audio_rec);
        isPlaying = false;
        mCurrentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED;
    }

    private void onPlayerStopped() {
        mPlayImageView.setImageResource(R.drawable.audio_play_dark);
        mRecImageView.setImageResource(R.drawable.audio_rec);

        setTime(0);
        new Handler().postDelayed(() -> {
            if (mSeekBar != null) {
                mSeekBar.setProgress(0);
            }
        }, 50);

        isPlaying = false;
    }
    //endregion

    //region UI updates
    private void initializeSeekBar() {

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pausePlaying(getContext(), true);
                }
                setTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Need to update the rec n play buttons
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAudioPlayer.setProgressTo(getContext(), seekBar.getProgress());
                onPlayerStarted();
            }
        });
    }

    private void setTime(int duration) {
        String timePattern = "mm:ss";
        if ((duration / (1000 * 60)) / 60 > 1) {
            timePattern = "HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timePattern, Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mTimeTextView.setText(simpleDateFormat.format(duration));
    }

    private void setSpeed() {
        mAudioPlayer.setSpeed(mSpeed);
        if (mSpeed % 1 == 0) {
            mSpeedTextView.setText(getString(R.string.set_speed, String.valueOf((int) mSpeed)));
        } else {
            mSpeedTextView.setText(getString(R.string.set_speed, String.valueOf(mSpeed)));
        }
    }

    //endregion

    //endregion

    public interface PlayerFragmentContainerCallbacks {
        void onPlayerClosed();
    }
}