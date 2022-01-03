package com.fluidtouch.noteshelf.document.dialogs.addnew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.audio.popup.FTAudioDialog;
import com.fluidtouch.noteshelf.clipart.FTClipartDialog;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddNewMediaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.camera)
    void onCameraClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_Camera");
        FTLog.crashlyticsLog("AddNew UI: Camera");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).pickFromCamera();
        dismiss();
    }

    @OnClick(R.id.photo_library)
    void onPhotoLibraryClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_Photo");
        FTLog.crashlyticsLog("AddNew UI: Photo Library");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).pickFromGallery();
        dismiss();
    }

    @OnClick(R.id.record_audio)
    void onRecordAudioClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_RecordAudio");
        FTLog.crashlyticsLog("AddNew UI: Record Audio");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).addNewAudio();
        dismiss();
    }

    @OnClick(R.id.audio_recordings)
    void onAudioRecordingsClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_AudioRecordings");
        FTLog.crashlyticsLog("AddNew UI: Audio Recordings");
        FTNoteshelfPage mCurrentPage = ((FTDocumentActivity) getActivity()).getCurrentPage();
        if (mCurrentPage != null) {
            ArrayList<Integer> pageNumbers = new ArrayList<>();
            List<FTAudioAnnotation> audioAnnotations = mCurrentPage.getParentDocument().getAudioAnnotations(getContext());
            for (int i = 0; i < audioAnnotations.size(); i++) {
                pageNumbers.add(((FTAudioAnnotationV1) audioAnnotations.get(i)).associatedPage.pageIndex() + 1);
            }
            FTAudioDialog.newInstance(audioAnnotations, pageNumbers, () -> {
                if (FTAddNewMediaFragment.this.getActivity() != null)
                    ((AddNewPopupListener) FTAddNewMediaFragment.this.getActivity()).addNewAudio();
            }, mCurrentPage.getParentDocument().resourceFolderItem().getFileItemURL()).show(getChildFragmentManager());
        }
    }

    @OnClick(R.id.media_library)
    void onMediaLibraryClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_MediaLib");
        FTLog.crashlyticsLog("AddNew UI: Media Library");
        new FTClipartDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.insert_from)
    void onInsertFromClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_InsertFrom");
        FTLog.crashlyticsLog("AddNew UI: Insert from...");
    }

    private void dismiss() {
        if (getParentFragment() != null) {
            ((DismissListener) getParentFragment()).dismiss();
        }
    }
}
