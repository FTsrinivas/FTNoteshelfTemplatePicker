package com.fluidtouch.noteshelf.document;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.document.imageedit.FTImageEditFragment;
import com.fluidtouch.noteshelf.document.textedit.FTEditTextFragment;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;

public abstract class FTAnnotationFragment extends Fragment {

    public static FTAnnotationFragment instance(FTAnnotation ftAnnotation, Callbacks callbacks) {
        if (ftAnnotation.annotationType() == FTAnnotationType.image) {
            return FTImageEditFragment.newInstance(ftAnnotation, callbacks);
        } else if (ftAnnotation.annotationType() == FTAnnotationType.text) {
            return FTEditTextFragment.newInstance(ftAnnotation, callbacks);
        }
        return null;
    }

    public abstract void outsideClick();

    public interface Callbacks {

        RectF getContainerRect();

        float getContainerScale();

        float getOriginalScale();

        RectF visibleFrame();

        RectF getVisibleRect();

        void addAnnotation(FTAnnotation annotation);

        void updateAnnotation(FTAnnotation oldAnnotation, FTAnnotation helperAnnotation);

        void removeAnnotation(FTAnnotation annotation);

        void onAnnotationEditFinish();

        void currentTextBoxCursorPosition(PointF pointF);

    }

}
