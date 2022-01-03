package com.fluidtouch.noteshelf.audio.player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.models.FTAudioTrack;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.io.IOException;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sreenu on 06/03/19
 */
public class FTAudioPlayer implements AudioManager.OnAudioFocusChangeListener {
    public static final String PLAYBACK_COMPLETION = "Playback completed";
    private static final String TAG = "FTAudioRecordPOC";
    private static final int SEEK_BAR_UPDATING_INTERVAL = 50;
    private static final int RECORDING_TIME_UPDATING_INTERVAL = 500;
    public static int currentAudioPageIndex = -1;
    private static FTAudioPlayer mInstance;
    public FTAudioRecording mRecording;
    public FTAudioTrack currentRecordingSession;
    public FTAudioPlayerStatus.FTPlayerMode currentMode = FTAudioPlayerStatus.FTPlayerMode.IDLE;
    private MediaRecorder recorder = null;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private int currentSession = 0;
    private int initialSeekBarDuration = 0;
    private float speed = 1;
    private String mRootPath = "";
    private AtomicInteger recordingTime = new AtomicInteger();
    private Handler mRecordingTimeHandler = new Handler();
    private Runnable mRecordingTimeRunnable;
    private boolean isAudioFocusAvailable = false;
    private ObservingService mAudioObservingService;
    private Handler mSeekBarHandler = new Handler();
    private Runnable mSeekBarRunnable;
    private AudioManager audioManager;
    private Context context;


    private FTAudioPlayer() {
        mAudioObservingService = new ObservingService();
    }

    public static synchronized FTAudioPlayer getInstance() {
        if (mInstance == null) {
            mInstance = new FTAudioPlayer();
        }
        return mInstance;
    }

    public static void showPlayerInProgressAlert(Context context, OnProgressAlertListener onProgressAlertListener) {
        FTDialogFactory.showAlertDialog(context, "", context.getString(R.string.audio_record_progress_alert_title),
                context.getString(R.string.stop_recording), context.getString(R.string.cancel), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        FTAudioPlayer.getInstance().stopRecording(context, true);
                        onProgressAlertListener.proceedForOperation();
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private void setRecording(FTAudioRecording recording, String rootPath) {
        mRecording = recording;
        mRootPath = rootPath;

        if (mRecording != null) {
            File file = new File(rootPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        if (context != null && isPlaying()) {
            stopPlaying(context, true);
        }
    }

    public int getCurrentSession() {
        return currentSession;
    }

    public void startPlaying(Context context, int index, int seekTo) {
        requestAudioFocus(context);
        if (index + 1 > mRecording.getTrackCount()) {
            stopPlaying(context, true);
            return;
        }

        if (mSeekBarRunnable == null) {
            initializeProgressRunnable(context);
        }

        currentSession = index;
        sendPlayerStatusBroadcast(context);
        isPlaying = true;

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getRootPath() + mRecording.getTrack(index).audioName);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(player -> {
                if (player != null) {
                    player.start();
                    player.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                    player.seekTo(seekTo - initialSeekBarDuration);
                    currentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS;
                }
            });
            mediaPlayer.setOnCompletionListener(player -> {
                initialSeekBarDuration += mRecording.getTrack(index).getDuration();
                startPlaying(context, index + 1, 0);
            });
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void pausePlaying(Context context, boolean shouldSendBroadcast) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            currentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED;
            if (shouldSendBroadcast) {
                sendPlayerStatusBroadcast(context, currentMode);
            }
        }
    }

    public void stopPlaying(Context context, boolean shouldSendBroadcast) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED;

            if (shouldSendBroadcast) {
                //Sending broadcast after the player finishes playing.
                Intent intent = new Intent(PLAYBACK_COMPLETION);
                intent.putExtra("path", getCurrentAudioName());
                context.sendBroadcast(intent);
            }

            removeProgressRunnable();
        }
        initialSeekBarDuration = 0;
        currentSession = 0;
    }

    public void startRecording(Context context) {
        currentRecordingSession = new FTAudioTrack();
        currentRecordingSession.setAudioName(FTDocumentUtils.getUDID() + ".mp3");
        currentRecordingSession.setStartTimeInterval(System.currentTimeMillis());

        String filePath = getRootPath() + currentRecordingSession.audioName;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();
            currentMode = FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS;
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
        }

        recordingTime = new AtomicInteger((int) getTotalDuration());
        addRecordingRunnable();
    }

    private void pauseRecording(Context context) {
        if (recorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.pause();
                currentMode = FTAudioPlayerStatus.FTPlayerMode.RECORDING_PAUSED;
                removeRecordingRunnable();
            } else {
                stopRecording(context, true);
            }
        }
    }

    private void resumeRecording(Context context) {
        if (recorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.resume();
                addRecordingRunnable();
            }
        } else {
            startRecording(context);
        }
    }

    public void stopRecording(Context context, boolean shouldSendBroadcast) {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException stopException) {
                //handle cleanup here
            }
            recorder.release();
            recorder = null;
            currentMode = FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED;
            if (shouldSendBroadcast) {
                sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.RECORDING_STOPPED);
            }
            removeRecordingRunnable();
        }

        if (currentRecordingSession != null) {
            currentRecordingSession.setEndTimeInterval(System.currentTimeMillis());
            mRecording.addTrack(currentRecordingSession);
            currentRecordingSession = null;
        }
    }

    public int getCurrentProgress() {
        if (mediaPlayer == null) {
            return initialSeekBarDuration;
        }
        return initialSeekBarDuration + mediaPlayer.getCurrentPosition();
    }

    public long getTotalDuration() {
        long duration = 0;
        if (mRecording != null) {
            for (int i = 0; i < mRecording.getTrackCount(); i++) {
                duration += mRecording.getTrack(i).getDuration();
            }
        }
        return duration;
    }

    public void setProgressTo(Context context, int progress) {
        int duration = 0;
        for (int i = 0; i < mRecording.getTrackCount(); i++) {
            int currentFileDuration = (int) mRecording.getTrack(i).getDuration();
            if (progress > duration + currentFileDuration) {
                duration += currentFileDuration;
            } else {
                initialSeekBarDuration = duration;
                startPlaying(context, i, progress);
                break;
            }
        }
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isRecording() {
        return recorder != null;
    }

    public String getRootPath() {
        return mRootPath;
    }

    private void sendPlayerStatusBroadcast(Context context) {
        sendPlayerStatusBroadcast(context, getPlayerMode());
    }

    public void sendPlayerStatusBroadcast(Context context, FTAudioPlayerStatus.FTPlayerMode mode) {
        if (mRecording != null && context != null) {
            FTAudioPlayerStatus status = new FTAudioPlayerStatus();
            status.audioRecordingName = getCurrentAudioName();
            status.setCurrentSessionIndex(getCurrentSession());
            status.setPlayerMode(mode);
            mAudioObservingService.postNotification(context.getString(R.string.intent_media_player_status), status);
        }
    }

    public int getCurrentSessionProgress() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    public void play(Context context, FTAudioRecording newRecording, String rootPath) {
        play(context, newRecording, rootPath, getCurrentSession());
    }

    public void play(Context context, FTAudioRecording newRecording, String rootPath, int currentSession) {
        if (getPlayerMode() != FTAudioPlayerStatus.FTPlayerMode.IDLE && mRecording != null && newRecording.fileName.equals(getCurrentAudioName())) {
            if (currentSession == getCurrentSession()) {
                startPlaying(context, currentSession, getCurrentSessionProgress());
            } else {
                play(context, currentSession);
            }
        } else {
            setRecording(newRecording, rootPath);
            sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.PLAYING_STARTED);
        }
    }

    public void play(Context context, int fromSession) {
        int seekBarProgress = 0;
        if (getCurrentSession() == fromSession) {
            seekBarProgress = getCurrentProgress();
        } else {
            for (int i = 0; i < fromSession; i++) {
                seekBarProgress += mRecording.getTrack(i).getDuration();
            }
        }

        pausePlaying(context, false);
        setProgressTo(context, seekBarProgress);
        //Need to send broadcast that player started from session
    }

    public void recordNewTrack(Context context, FTAudioRecording recording, String rootPath) {
        stopPlaying(context, false);
        stopRecording(context, true);

        setRecording(recording, rootPath);
        sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.RECORDING_STARTED);
    }

    public FTAudioPlayerStatus.FTPlayerMode getPlayerMode() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                return FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS;
            } else if (!mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() > 1) {
                return FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED;
            } else {
                return FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED;
            }
        }
        return FTAudioPlayerStatus.FTPlayerMode.IDLE;
    }

    private void initializeProgressRunnable(Context context) {
        mSeekBarHandler = new Handler();
        mSeekBarRunnable = () -> {
            if (isPlaying()) {
                sendPlayerStatusBroadcast(context);
            }
            mSeekBarHandler.postDelayed(mSeekBarRunnable, SEEK_BAR_UPDATING_INTERVAL);
        };
        mSeekBarHandler.postDelayed(mSeekBarRunnable, SEEK_BAR_UPDATING_INTERVAL);
    }

    private void removeProgressRunnable() {
        if (mSeekBarHandler != null) {
            mSeekBarHandler.removeCallbacks(mSeekBarRunnable);
            mSeekBarRunnable = null;
        }
    }

    private void addRecordingRunnable() {
        mRecordingTimeHandler = new Handler();
        mRecordingTimeRunnable = () -> {
            recordingTime.set((int) (getTotalDuration() + System.currentTimeMillis() - currentRecordingSession.startTimeInterval));
            sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS);
            mRecordingTimeHandler.postDelayed(mRecordingTimeRunnable, RECORDING_TIME_UPDATING_INTERVAL);
        };
        mRecordingTimeHandler.postDelayed(mRecordingTimeRunnable, RECORDING_TIME_UPDATING_INTERVAL);
    }

    private void removeRecordingRunnable() {
        if (mRecordingTimeHandler != null) {
            mRecordingTimeHandler.removeCallbacks(mRecordingTimeRunnable);
            mRecordingTimeHandler = null;
        }
    }

    public String getCurrentAudioName() {
        if (mRecording != null) {
            return mRecording.fileName;
        }
        return "";
    }

    public int getRecordingTime() {
        return recordingTime.get();
    }

    public boolean requestAudioFocus(Context context) {
        if (isAudioFocusAvailable) {
            return true;
        }
        removeAudioFocus();

        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        int result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();

            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isAudioFocusAvailable = true;
            return true;
        }
        return false;
    }

    public boolean removeAudioFocus() {
        if (audioManager != null) {
            isAudioFocusAvailable = false;
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
        }

        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                isAudioFocusAvailable = true;
                if (currentMode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED) {
                    startPlaying(this.context, currentSession, getCurrentSessionProgress());
                } else if (currentMode == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PAUSED) {
                    resumeRecording(context);
                }
                if (mediaPlayer != null) {
//                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                isAudioFocusAvailable = false;
                if (currentMode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS) {
                    pausePlaying(context, true);
                    sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED);
                } else if (currentMode == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS) {
                    pauseRecording(context);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                isAudioFocusAvailable = false;
                if (currentMode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS) {
                    pausePlaying(context, true);
                    sendPlayerStatusBroadcast(context, FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED);
                } else if (currentMode == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS) {
                    pauseRecording(context);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (currentMode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_PROGRESS) {
//                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    public void addObserver(Context context, Observer observer) {
        mAudioObservingService.addObserver(context.getString(R.string.intent_media_player_status), observer);
    }

    public void removeObserver(Context context, Observer observer) {
        mAudioObservingService.removeObserver(context.getString(R.string.intent_media_player_status), observer);
    }

    public interface OnProgressAlertListener {
        void proceedForOperation();
    }
}
