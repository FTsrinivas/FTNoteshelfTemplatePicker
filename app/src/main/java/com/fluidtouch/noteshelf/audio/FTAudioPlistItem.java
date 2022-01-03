package com.fluidtouch.noteshelf.audio;

import android.content.Context;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.models.FTAudioTrack;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreenu on 11/03/19
 */
public class FTAudioPlistItem extends FTFileItemPlist {
    private static final String RECORDING_MODEL_KEY = "recordingModel";
    private static final String AUDIO_TRACKS_KEY = "audioTracks";
    private static final String AUDIO_NAME_KEY = "audioName";
    private FTAudioRecording audioRecording;
    private List<FTAudioTrack> audioTracks;


    public FTAudioPlistItem(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTAudioPlistItem(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public void setAudioRecording(FTAudioRecording recording) {
        if (this.audioRecording == null) {
            this.audioRecording = recording;
            this.updateContent(new NSDictionary());
        } else {
            this.audioRecording = recording;
            this.isModified = true;
        }
    }

    public List<FTAudioTrack> getAudioTracks(Context context) {
        if (this.audioTracks == null || (this.audioTracks != null && this.audioTracks.isEmpty())) {
            List<FTAudioTrack> audioTracks = new ArrayList<>();
            NSDictionary rootDict = this.contentDictionary(context);
            if (rootDict.containsKey(RECORDING_MODEL_KEY) && rootDict.objectForKey(RECORDING_MODEL_KEY) != null) {
                NSDictionary tracksDict = (NSDictionary) rootDict.objectForKey(RECORDING_MODEL_KEY);
                if (tracksDict.containsKey(AUDIO_TRACKS_KEY) && tracksDict.objectForKey(AUDIO_TRACKS_KEY) != null) {
                    NSArray localTracks = (NSArray) tracksDict.objectForKey(AUDIO_TRACKS_KEY);
                    for (int i = 0; i < localTracks.count(); i++) {
                        NSDictionary dict = (NSDictionary) localTracks.objectAtIndex(i);
                        FTAudioTrack track = new FTAudioTrack(dict);
                        audioTracks.add(track);
                    }
                }
            }

            this.audioTracks = audioTracks;
        }
        return this.audioTracks;
    }

    public void insertTrack(Context context, FTAudioTrack track) {
        if (getAudioTracks(context) != null) {
            this.audioTracks.add(track);
            this.isModified = true;
        }
    }

    @Override
    public Boolean deleteFileItem() {
        if (audioRecording != null) {
            for (int i = 0; i < audioRecording.getAudioTracks().size(); i++) {
                File file = new File(this.parent.getFileItemURL().getPath() + "/" + audioRecording.getAudioTracks().get(i).audioName);
                file.delete();
            }
        }
        return super.deleteFileItem();
    }

    @Override
    public Boolean saveContentsOfFileItem(Context context) {
        this.setObject(context, audioRecording.fileName, AUDIO_NAME_KEY);
        this.setObject(context, audioRecording.dictionaryRepresentation(), RECORDING_MODEL_KEY);
        return super.saveContentsOfFileItem(context);
    }

    public FTAudioRecording getRecording(Context context) {
        if (this.audioRecording == null) {
            FTAudioRecording recording = new FTAudioRecording();
            List<FTAudioTrack> tracks = new ArrayList<>();
            NSDictionary rootDict = this.contentDictionary(context);
            if (rootDict.containsKey(RECORDING_MODEL_KEY) && rootDict.objectForKey(RECORDING_MODEL_KEY) != null) {
                recording.setFileName(rootDict.objectForKey(AUDIO_NAME_KEY).toString());
                NSDictionary tracksDict = (NSDictionary) rootDict.objectForKey(RECORDING_MODEL_KEY);
                if (tracksDict.containsKey(AUDIO_TRACKS_KEY) && tracksDict.objectForKey(AUDIO_TRACKS_KEY) != null) {
                    NSArray localTracks = (NSArray) tracksDict.objectForKey(AUDIO_TRACKS_KEY);
                    for (int i = 0; i < localTracks.count(); i++) {
                        NSDictionary dict = (NSDictionary) localTracks.objectAtIndex(i);
                        FTAudioTrack track = new FTAudioTrack(dict);
                        tracks.add(track);
                    }
                }
                recording.setAudioTracks(tracks);
            }

            this.audioRecording = recording;
        }
        return this.audioRecording;
    }
}
