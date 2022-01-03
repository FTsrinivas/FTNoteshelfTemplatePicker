package com.fluidtouch.noteshelf.audio.models;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreenu on 06/03/19
 */
public class FTAudioRecording implements Serializable {
    public String fileName;

    private List<FTAudioTrack> audioTracks = new ArrayList<>();

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FTAudioTrack getTrack(int index) {
        return audioTracks.get(index);
    }

    public void addTrack(FTAudioTrack track) {
        audioTracks.add(track);
    }

    public int getTrackCount() {
        return audioTracks.size();
    }

    public List<FTAudioTrack> getAudioTracks() {
        return audioTracks;
    }

    public void setAudioTracks(List<FTAudioTrack> audioTracks) {
        this.audioTracks = audioTracks;
    }

    public NSDictionary dictionaryRepresentation() {
        NSDictionary recordingModel = new NSDictionary();

        NSArray tracks;
        if (audioTracks != null && audioTracks.size() > 0) {
            tracks = new NSArray(audioTracks.size());
            for (int i = 0; i < audioTracks.size(); i++) {
                NSDictionary dict = audioTracks.get(i).dictionaryRepresentation();
                tracks.setValue(i, dict);
            }
        } else {
            tracks = new NSArray(0);
        }

        recordingModel.put("audioTracks", tracks);
        return recordingModel;
    }

    public FTAudioRecording deepCopy() {
        FTAudioRecording recording = new FTAudioRecording();
        for (int i = 0; i < audioTracks.size(); i++) {
            recording.addTrack(audioTracks.get(i).deepCopy());
        }
        return recording;
    }
}
