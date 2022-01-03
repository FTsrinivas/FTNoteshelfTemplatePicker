package com.fluidtouch.noteshelf.audio;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.util.SizeF;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import java.util.List;

import static com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED;

/**
 * Created by Sreenu on 29/03/19
 */
public class FTAudioView extends AppCompatImageView {
    private FTAudioAnnotationV1 mAnnotation;
    private FTAudioViewCallbacks mParentCallbacks;
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
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getLayoutParams();

                    xParentInitial = x;
                    yParentInitial = y;

                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;

                    return true;

                case MotionEvent.ACTION_UP:
                    if (x == xParentInitial && y == yParentInitial) {
                        return false;
                    }
                    onActionUp();
                    float lengthUp = 54 * getContext().getResources().getDisplayMetrics().density * mParentCallbacks.getContainerScale();
                    FrameLayout.LayoutParams layoutParamsUp = (FrameLayout.LayoutParams) getLayoutParams();
                    mCurrentBoundingRect = new RectF(layoutParamsUp.leftMargin, layoutParamsUp.topMargin, layoutParamsUp.leftMargin + lengthUp, layoutParamsUp.topMargin + lengthUp);
                    mParentCallbacks.reloadInRect(mCurrentBoundingRect);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
                    int bundX = x - xDelta;
                    int bundY = y - yDelta;
                    float length = 54 * getContext().getResources().getDisplayMetrics().density * mParentCallbacks.getContainerScale();
                    SizeF sizeF = new SizeF(length, length);
                    RectF scaledContainerRect = FTGeometryUtils.scaleRect(mParentCallbacks.getContainerRect(), mParentCallbacks.getContainerScale());
                    if (bundX >= 0 && bundX <= scaledContainerRect.width() - sizeF.getWidth()) {
                        layoutParams.leftMargin = x - xDelta;
                    }
                    if (bundY >= 0 && bundY <= scaledContainerRect.height() - sizeF.getHeight()) {
                        layoutParams.topMargin = y - yDelta;
                    }

                    layoutParams.rightMargin = 0;
                    layoutParams.bottomMargin = 0;
                    setLayoutParams(layoutParams);
                    return true;

                default:
                    break;
            }

            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                Log.i("FTZoomLayout", "Action Cancel");
                mParentCallbacks.setAllowZoom(true);
            }
            return false;
        }
    };

    public FTAudioView(Context context, FTAnnotation ftAnnotation, FTAudioViewCallbacks callbacks) {
        super(context);

        setImageResource(R.drawable.recording);
        mAnnotation = (FTAudioAnnotationV1) ftAnnotation;
        mParentCallbacks = callbacks;
        mCurrentScale = mParentCallbacks.getContainerScale();

        float length = getResources().getDimensionPixelOffset(R.dimen._60dp) * mParentCallbacks.getContainerScale();


        if (mAnnotation.getBoundingRect().width() == 0) {
            mCurrentBoundingRect = FTGeometryUtils.scaleRect(getNewAudioBoundingRect(), mParentCallbacks.getContainerScale());
            setLayoutParams(new FrameLayout.LayoutParams((int) length, (int) length));
        } else {
            mCurrentBoundingRect = FTGeometryUtils.scaleRect(mAnnotation.getBoundingRect(), mParentCallbacks.getContainerScale());
            setLayoutParams(new FrameLayout.LayoutParams((int) (mCurrentBoundingRect.width()), (int) (mCurrentBoundingRect.height())));
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = (int) (mCurrentBoundingRect.left);
        layoutParams.topMargin = (int) (mCurrentBoundingRect.top);
        setLayoutParams(layoutParams);

        mSingleTapGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                showAudioSingleTapOptions(FTAudioView.this);
                return true;
            }
        });

        setOnTouchListener(mParentOnTouchListener);
    }

    private void onActionUp() {
        mParentCallbacks.setAllowZoom(true);
        if (mParentCallbacks.isTextInEditMode()) {
            mParentCallbacks.setAllowZoom(false);
        }

        try {
            Object tag = ((FrameLayout) getParent()).getChildAt(1).getTag();
            if (tag != null && tag.equals(getContext().getString(R.string.tag_lasso_container))) {
                mParentCallbacks.setAllowZoom(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mParentCallbacks.setAudioMode(false);
    }

    private boolean isIntersectingTheExistingAudio(List<FTAudioAnnotation> audioAnnotations, RectF newRectF) {
        for (int i = 0; i < audioAnnotations.size(); i++) {
            if (audioAnnotations.get(i).getBoundingRect().intersect(newRectF)) {
                return true;
            }
        }
        return false;
    }

    private RectF getNewAudioBoundingRect() {
        List<FTAudioAnnotation> audioAnnotations = mParentCallbacks.currentPage().getAudioAnnotations();
        float padding = 20 * getResources().getDisplayMetrics().density;
        float containerWidth = mParentCallbacks.getContainerRect().width();
        float length = getResources().getDimensionPixelOffset(R.dimen._60dp);
        RectF newRectF = new RectF(containerWidth - length - padding, padding, containerWidth - padding, padding + length);

        while (isIntersectingTheExistingAudio(audioAnnotations, newRectF)) {
            newRectF.offset(0, length);
            if (newRectF.bottom > mParentCallbacks.getContainerRect().height()) {
                newRectF.offset(-length, -((((int) (mParentCallbacks.getContainerRect().height() - padding)) / length) * (length - 1)));
                if (newRectF.left < mParentCallbacks.getContainerRect().left) {
                    newRectF = new RectF(containerWidth - length - padding, padding, containerWidth - padding, padding + length);
                    break;
                }
            }
        }

        return newRectF;
    }

    public void outsideClick() {
        saveAudio();
        mParentCallbacks.onAudioEditFinish();
    }

    private RectF getContainerScaledRect() {
        RectF rectF = new RectF();
        rectF.left = getX();
        rectF.right = rectF.left + getWidth();
        rectF.top = getY();
        rectF.bottom = rectF.top + getHeight();
        return FTGeometryUtils.scaleRect(rectF, 1 / mParentCallbacks.getContainerScale());
    }

    private void saveAudio() {
        boolean isNewAnnotation = mAnnotation.getBoundingRect().width() == 0;

        if (isNewAnnotation) {
            mAnnotation.setBoundingRect(getContainerScaledRect());
            mParentCallbacks.addAnnotation(mAnnotation);
        } else {
            mAnnotation.setAudioRecording(mAnnotation.getAudioRecording());
            mAnnotation.setBoundingRect(getContainerScaledRect());
            mParentCallbacks.updateAnnotation(mAnnotation, mAnnotation);
        }
    }

    private void showAudioSingleTapOptions(View anchor) {
        final int delay = 300;
        final int onClickingAlpha = 180;
        final int finalAlpha = 255;
        mOptionsPopupWindow = new PopupWindow(getContext());

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.audio_options, null);

        mOptionsPopupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setContentView(popupView);
        mOptionsPopupWindow.setFocusable(true);
        mOptionsPopupWindow.setBackgroundDrawable(null);
        TextView playPauseTextView = mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_play_pause_text_view);
        TextView recordTextView = mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_record_text_view);
        if (FTAudioPlayer.getInstance().isPlaying()) {
            playPauseTextView.setText(getContext().getString(R.string.pause));
        } else {
            playPauseTextView.setText(getContext().getString(R.string.play));
            if (FTAudioPlayer.getInstance().isRecording()) {
                recordTextView.setText(getContext().getString(R.string.stop));
                if (FTAudioPlayer.getInstance().mRecording.getTrackCount() < 1) {
                    playPauseTextView.setVisibility(View.GONE);
                }
            } else {
                recordTextView.setText(getContext().getString(R.string.record));
                playPauseTextView.setVisibility(View.VISIBLE);
            }
        }

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_record_text_view).setOnClickListener(v -> {
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FTAudioPlayer.getInstance().isRecording()) {
                        FTAudioPlayer.getInstance().stopRecording(getContext(), true);
                    } else {
                        FTAudioPlayer.getInstance().recordNewTrack(getContext(), mAnnotation.getAudioRecording(), FTAudioPlayer.getInstance().getRootPath());
                    }
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_delete_text_view).setOnClickListener(v -> {
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAnnotation.delete();
                    mParentCallbacks.removeAnnotation(mAnnotation);
                    mParentCallbacks.onAudioEditFinish();
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.audio_options_play_pause_text_view).setOnClickListener(v -> {
            v.getBackground().setAlpha(onClickingAlpha);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FTAudioPlayer.getInstance().isPlaying()) {
                        FTAudioPlayer.getInstance().pausePlaying(getContext(), true);
                        FTAudioPlayer.getInstance().sendPlayerStatusBroadcast(getContext(), PLAYING_PAUSED);
                    } else {
                        if (FTAudioPlayer.getInstance().isRecording()) {
                            FTAudioPlayer.getInstance().stopRecording(getContext(), false);
                        }
                        FTAudioPlayer.getInstance().play(getContext(), FTAudioPlayer.getInstance().mRecording, FTAudioPlayer.getInstance().getRootPath());
                    }
                    v.getBackground().setAlpha(finalAlpha);
                    mOptionsPopupWindow.dismiss();
                }
            }, delay);
        });

        mOptionsPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onActionUp();
            }
        });

        mOptionsPopupWindow.showAsDropDown(anchor, 0, (int) mParentCallbacks.getVisibleRect().top);
    }

    public void onLayoutChanged() {
        mCurrentBoundingRect = FTGeometryUtils.scaleRect(mCurrentBoundingRect, mParentCallbacks.getContainerScale() / mCurrentScale);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = (int) (mCurrentBoundingRect.left);
        layoutParams.topMargin = (int) (mCurrentBoundingRect.top);
        layoutParams.width = (int) mCurrentBoundingRect.width();
        layoutParams.height = (int) mCurrentBoundingRect.height();
        setLayoutParams(layoutParams);
        mCurrentScale = mParentCallbacks.getContainerScale();
    }

    public RectF getBoundingRect() {
        return mCurrentBoundingRect;
    }

    public String getCurrentUUID() {
        return mAnnotation.uuid;
    }

    public interface FTAudioViewCallbacks {

        RectF getContainerRect();

        RectF getVisibleRect();

        float getContainerScale();

        void addAnnotation(FTAnnotation annotation);

        void updateAnnotation(FTAnnotation oldAnnotation, FTAnnotation helperAnnotation);

        void removeAnnotation(FTAnnotation annotation);

        void setAllowZoom(boolean allowZoom);

        void onAudioEditFinish();

        void reloadInRect(RectF rectF);

        boolean isTextInEditMode();

        void setAudioMode(boolean enabled);

        FTNoteshelfPage currentPage();
    }
}
