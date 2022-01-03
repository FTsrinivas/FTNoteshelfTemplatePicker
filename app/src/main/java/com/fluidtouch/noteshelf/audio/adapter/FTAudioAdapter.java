package com.fluidtouch.noteshelf.audio.adapter;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 13/03/19
 */
public class FTAudioAdapter extends BaseRecyclerAdapter<FTAudioRecording, FTAudioAdapter.FTAudioViewHolder> {
    private final List<Integer> mPageNumbers;
    private String currentPlayingAudio = "";
    private int currentPlayingAudioIndex = -1;
    private FTAudioContainerToAdapterListener mContainerCallback;

    public FTAudioAdapter(List<Integer> pageNumbers, FTAudioContainerToAdapterListener audioContainerToAdapterListener) {
        this.mPageNumbers = pageNumbers;
        this.mContainerCallback = audioContainerToAdapterListener;
        if (FTAudioPlayer.getInstance().isPlaying()) {
            currentPlayingAudio = FTAudioPlayer.getInstance().getCurrentAudioName();
        }
    }

    @NonNull
    @Override
    public FTAudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FTAudioViewHolder(getView(parent, R.layout.item_audio_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull FTAudioViewHolder holder, int position) {
        FTAudioRecording item = getItem(position);
        holder.mNameTextView.setText(mContainerCallback.getCreatedTime(position));
        holder.mPageNumberTextView.setText(" · p. " + mPageNumbers.get(position));

        int playPauseDrawable = R.drawable.recording_play;
        int timeColor = android.R.color.black;
        float timeTextAlpha = 0.5f;
        String time = getTime(getTotalDuration(item));

        if (FTAudioPlayer.getInstance().mRecording != null && FTAudioPlayer.getInstance().getCurrentAudioName().equals(item.fileName)) {
            if (FTAudioPlayer.getInstance().currentMode == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS) {
                timeColor = R.color.audio_recording_time_color;
                time = getTime(FTAudioPlayer.getInstance().getRecordingTime());
                currentPlayingAudioIndex = position;
            } else if (FTAudioPlayer.getInstance().isPlaying()) {
                currentPlayingAudioIndex = position;
                playPauseDrawable = R.drawable.recording_pause;
                timeColor = R.color.blue;
                timeTextAlpha = 1.0f;
                time = getTime(FTAudioPlayer.getInstance().getCurrentProgress());
            }
        }
        holder.mPlayPauseImageView.setImageResource(playPauseDrawable);
        holder.mTimeTextView.setText(time);
        holder.mTimeTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), timeColor));
        holder.mTimeTextView.setAlpha(timeTextAlpha);
    }

    private String getTime(long duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m'm':ss's'", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(duration);
    }

    private long getTotalDuration(FTAudioRecording item) {
        if (item == null) {
            return 0;
        }

        long duration = 0;
        for (int i = 0; i < item.getTrackCount(); i++) {
            duration += item.getTrack(i).getDuration();
        }

        return duration;
    }

    public void applyDataChanges(FTAudioPlayerStatus status) {
        notifyItemChanged(currentPlayingAudioIndex);
        if (!FTAudioPlayer.getInstance().isPlaying() && !FTAudioPlayer.getInstance().isRecording()) {
            currentPlayingAudio = "";
            currentPlayingAudioIndex = -1;
        }
    }

    public interface FTAudioContainerToAdapterListener {
        void showSessions(FTAudioRecording audioRecording);

        void playAudio(FTAudioRecording audioRecording);

        void onResume();

        void onDestroy();

        String getCreatedTime(int position);
    }

    class FTAudioViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_audio_recordings_name_text_view)
        protected TextView mNameTextView;
        @BindView(R.id.item_audio_recordings_time_text_view)
        protected TextView mTimeTextView;
        @BindView(R.id.item_audio_recordings_page_number_text_view)
        protected TextView mPageNumberTextView;
        @BindView(R.id.item_audio_recordings_play_pause_image_view)
        protected ImageView mPlayPauseImageView;
        @BindView(R.id.item_audio_recordings_play_pause_layout)
        RelativeLayout mPlayPauseLayout;
        @BindView(R.id.item_audio_recordings_progress_bar)
        ProgressBar mProgressBar;

        public FTAudioViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mPageNumberTextView.setVisibility(View.VISIBLE);
        }

        @OnClick(R.id.item_audio_recordings_play_pause_layout)
        void onPlayPauseTapped() {
            if (FTAudioPlayer.getInstance().isRecording()) {
                FTAudioPlayer.showPlayerInProgressAlert(itemView.getContext(), () -> {
                    FTAudioPlayer.getInstance().stopRecording(itemView.getContext(), true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onPlayPauseTapped();
                        }
                    }, 200);
                });
                return;
            }
            if (getAdapterPosition() == -1) {
                return;
            }
            if (currentPlayingAudioIndex != -1 && currentPlayingAudioIndex != getAdapterPosition()) {
                notifyItemChanged(currentPlayingAudioIndex);
            }

            if (currentPlayingAudio.equals(getItem(getAdapterPosition()).fileName)) {
                currentPlayingAudio = "";
                currentPlayingAudioIndex = -1;
                FTAudioPlayer.getInstance().pausePlaying(itemView.getContext(), true);
            } else {
                currentPlayingAudio = getItem(getAdapterPosition()).fileName;
                currentPlayingAudioIndex = getAdapterPosition();
                mContainerCallback.playAudio(getItem(getAdapterPosition()));
            }

            notifyItemChanged(getAdapterPosition());
        }

        @OnClick(R.id.item_audio_parent_layout)
        void showSessions() {
            mContainerCallback.showSessions(getItem(getAdapterPosition()));
        }
    }
}