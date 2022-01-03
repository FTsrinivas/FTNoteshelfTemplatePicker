package com.fluidtouch.noteshelf.audio;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.SizeF;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.utils.FTPopupFactory;
import com.fluidtouch.noteshelf.document.FTAnnotationFragment;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED;

/**
 * Created by Sreenu on 13/03/19
 */

/**
 * We are not using this class.
 */
public class FTAudioFragment extends FTAnnotationFragment {
    @BindView(R.id.audio_image_view)
    protected ImageView mAudioImageView;

    private FTAudioAnnotationV1 mAnnotation;
    private Callbacks mParentCallbacks;
    private GestureDetector mSingleTapGestureDetector;
    private PopupWindow mOptionsPopupWindow;
    private float mCurrentScale;
    private RectF mCurrentBoundingRect;

    private View.OnTouchListener mParentOnTouchListener = new View.OnTouchListener() {
        private int xDelta;
        private int yDelta;

        private float xParentInitial;
        private float yParentInitial;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mSingleTapGestureDetector.onTouchEvent(event);
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();

                    xParentInitial = x;
                    yParentInitial = y;

                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;

                    return true;

                case MotionEvent.ACTION_UP:
                    if (x == xParentInitial && y == yParentInitial) {
                        return false;
                    }

                    return true;

                case MotionEvent.ACTION_MOVE:
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
                    int bundX = x - xDelta;
                    int bundY = y - yDelta;
                    float length = 54 * getContext().getResources().getDisplayMetrics().density;
                    SizeF sizeF = new SizeF(length, length);
                    RectF scaledContainerRect = FTGeometryUtils.scaleRect(mParentCallbacks.getContainerRect(), mParentCallbacks.getContainerScale());
                    if (bundX >= 0 && bundX <= scaledContainerRect.width() - sizeF.getWidth()) {
                        layoutParams.leftMargin = x - xDelta;
                    }
                    if (bundY >= 0 && bundX <= scaledContainerRect.height() - sizeF.getHeight()) {
                        layoutParams.topMargin = y - yDelta;
                    }

                    layoutParams.rightMargin = 0;
                    layoutParams.bottomMargin = 0;
                    mCurrentBoundingRect = new RectF(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.leftMargin + length, layoutParams.topMargin + length);
                    getView().setLayoutParams(layoutParams);
                    return true;

                default:
                    break;
            }
            return false;
        }
    };

    public static FTAudioFragment newInstance(FTAnnotation ftAnnotation, FTAnnotationFragment.Callbacks callbacks) {
        FTAudioFragment ftAudioFragment = new FTAudioFragment();
        ftAudioFragment.mAnnotation = (FTAudioAnnotationV1) ftAnnotation;
        ftAudioFragment.mParentCallbacks = callbacks;
        return ftAudioFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mCurrentScale = mParentCallbacks.getContainerScale();

//        FTAudioPlayer.getInstance().play(getContext(), mAnnotation.getAudioRecording(),
//                mAnnotation.associatedPage.parentDocument.resourceFolderItem().getFileItemURL().getPath() + "/");
        float length = view.getContext().getResources().getDimensionPixelOffset(R.dimen.margin_fifty_four) * mParentCallbacks.getContainerScale();
        view.setLayoutParams(new FrameLayout.LayoutParams((int) length, (int) length));

        if (mAnnotation.getBoundingRect().width() == 0) {
//            mCurrentBoundingRect = FTGeometryUtils.scaleRect(mParentCallbacks.getNewAudioBoundingRect(), mParentCallbacks.getContainerScale());
        } else {
            mCurrentBoundingRect = FTGeometryUtils.scaleRect(mAnnotation.getBoundingRect(), mParentCallbacks.getContainerScale());
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = (int) (mCurrentBoundingRect.left);
        layoutParams.topMargin = (int) (mCurrentBoundingRect.top);
        view.setLayoutParams(layoutParams);

        mSingleTapGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                showAudioSingleTapOptions(view);
                return true;
            }
        });

        view.setOnTouchListener(mParentOnTouchListener);
    }

    @Override
    public void outsideClick() {
        saveAudio();
        mParentCallbacks.onAnnotationEditFinish();
    }

    private RectF getContainerScaledRect() {
        RectF rectF = new RectF();
        rectF.left = getView().getX();
        rectF.right = rectF.left + getView().getWidth();
        rectF.top = getView().getY();
        rectF.bottom = rectF.top + getView().getHeight();
        return FTGeometryUtils.scaleRect(rectF, 1 / mParentCallbacks.getContainerScale());
    }

    private void saveAudio() {
        boolean isNewAnnotation = mAnnotation.getBoundingRect().width() == 0;

        if (isNewAnnotation) {
            mAnnotation.setAudioRecording(mAnnotation.getAudioRecording());
            mAnnotation.setBoundingRect(getContainerScaledRect());
            mParentCallbacks.addAnnotation(mAnnotation);
        } else {
            FTAudioAnnotationV1 helperAnnotation = new FTAudioAnnotationV1(getContext(), mAnnotation.associatedPage);
            helperAnnotation.setAudioRecording(mAnnotation.getAudioRecording().deepCopy());
            helperAnnotation.setBoundingRect(getContainerScaledRect());
            mParentCallbacks.updateAnnotation(mAnnotation, helperAnnotation);
        }
    }

    private void showAudioSingleTapOptions(View view) {
        final int delay = 300;
        final int onClickingAlpha = 180;
        final int finalAlpha = 255;
        mOptionsPopupWindow = FTPopupFactory.create(getContext(), view, R.layout.audio_options, 0, 0);

        TextView playPauseTextView = mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_play_pause_text_view);
        if (FTAudioPlayer.getInstance().isPlaying()) {
            playPauseTextView.setText(getString(R.string.pause));
        } else {
            playPauseTextView.setText(getString(R.string.play));
        }

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_record_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Audio_Record");
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FTAudioPlayer.getInstance().recordNewTrack(getContext(), mAnnotation.getAudioRecording(), FTAudioPlayer.getInstance().getRootPath());
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_delete_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Audio_Delete");
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAnnotation.delete();
                    mParentCallbacks.removeAnnotation(mAnnotation);
                    mParentCallbacks.onAnnotationEditFinish();
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_play_pause_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Audio_Play");
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FTAudioPlayer.getInstance().isPlaying()) {
                        FTAudioPlayer.getInstance().pausePlaying(getContext(), true);
                        FTAudioPlayer.getInstance().sendPlayerStatusBroadcast(getContext(), PLAYING_PAUSED);
                    } else {
                        FTAudioPlayer.getInstance().play(getContext(), FTAudioPlayer.getInstance().mRecording, FTAudioPlayer.getInstance().getRootPath());
                    }
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.showAsDropDown(view);
    }

    public void onLayoutChanged() {
        if (getView() != null) {
            mCurrentBoundingRect = FTGeometryUtils.scaleRect(mCurrentBoundingRect, mParentCallbacks.getContainerScale() / mCurrentScale);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
            layoutParams.leftMargin = (int) (mCurrentBoundingRect.left);
            layoutParams.topMargin = (int) (mCurrentBoundingRect.top);
            getView().setLayoutParams(layoutParams);
            mCurrentScale = mParentCallbacks.getContainerScale();
        }
    }
}
