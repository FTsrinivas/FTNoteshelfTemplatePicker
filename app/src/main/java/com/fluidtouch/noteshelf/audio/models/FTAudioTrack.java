package com.fluidtouch.noteshelf.audio.models;

import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;

import java.io.Serializable;

/**
 * Created by Sreenu on 06/03/19
 */
public class FTAudioTrack implements Serializable {
    public String audioName;
    public long startTimeInterval;
    private long endTimeInterval;

    public FTAudioTrack() {
    }

    public FTAudioTrack(NSDictionary dict) {
        this.audioName = dict.objectForKey("audioName").toString();
        this.startTimeInterval = ((Long) dict.objectForKey("startTimeInterval").toJavaObject());
        this.endTimeInterval = ((Long) dict.objectForKey("endTimeInterval").toJavaObject());
    }

    public void setStartTimeInterval(long startTimeInterval) {
        this.startTimeInterval = startTimeInterval;
    }

    public void setEndTimeInterval(long endTimeInterval) {
        this.endTimeInterval = endTimeInterval;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public long getDuration() {
        return endTimeInterval - startTimeInterval;
    }

    public NSDictionary dictionaryRepresentation() {
        NSDictionary dict = new NSDictionary();
        dict.put("audioName", audioName);
        dict.put("startTimeInterval", startTimeInterval);
        dict.put("endTimeInterval", endTimeInterval);
        return dict;
    }

    public FTAudioTrack deepCopy() {
        FTAudioTrack track = new FTAudioTrack();
        track.audioName = FTDocumentUtils.getUDID() + ".mp3";
        track.startTimeInterval = startTimeInterval;
        track.endTimeInterval = endTimeInterval;
        return track;
    }
}
