package com.fluidtouch.noteshelf.audio.models;

import java.io.Serializable;

/**
 * Created by Sreenu on 11/03/19
 */
public class FTAudioPlayerStatus implements Serializable {
    public String audioRecordingName;
    private int currentSessionIndex = -1;
    private FTPlayerMode playerMode = FTPlayerMode.PLAYING_STOPPED;

    public int getCurrentSessionIndex() {
        return currentSessionIndex;
    }

    public void setCurrentSessionIndex(int currentSessionIndex) {
        this.currentSessionIndex = currentSessionIndex;
    }

    public FTPlayerMode getPlayerMode() {
        return playerMode;
    }

    public void setPlayerMode(FTPlayerMode playerMode) {
        this.playerMode = playerMode;
    }

    public enum FTPlayerMode {
        IDLE,
        RECORDING_STARTED, RECORDING_PROGRESS, RECORDING_PAUSED, RECORDING_STOPPED,
        PLAYING_STARTED, PLAYING_PROGRESS, PLAYING_PAUSED, PLAYING_STOPPED
    }
}
