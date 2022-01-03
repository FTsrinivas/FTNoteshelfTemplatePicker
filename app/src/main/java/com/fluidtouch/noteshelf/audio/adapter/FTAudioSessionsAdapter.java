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
import com.fluidtouch.noteshelf.audio.models.FTAudioTrack;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 07/03/19
 */
public class FTAudioSessionsAdapter extends BaseRecyclerAdapter<FTAudioTrack, FTAudioSessionsAdapter.RecordingsViewHolder> {
    private int currentPlayingSession = -1;
    private FTSessionsContainerToAdapterCallback mContainerCallback;

    public FTAudioSessionsAdapter(FTSessionsContainerToAdapterCallback containerCallback) {
        if (FTAudioPlayer.getInstance().getPlayerMode() != FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED) {
            currentPlayingSession = containerCallback.getCurrentSession();
        }
        mContainerCallback = containerCallback;
    }

    @NonNull
    @Override
    public RecordingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecordingsViewHolder(getView(parent, R.layout.item_audio_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingsViewHolder holder, int position) {
        FTAudioTrack session = getItem(position);
        holder.mNameTextView.setText(holder.itemView.getContext().getString(R.string.set_session, position));

        int playPauseDrawable = R.drawable.recording_play;
        int progressBarVisibility = View.INVISIBLE;
        int timeColor = android.R.color.black;
        float timeTextAlpha = 0.5f;
        String time = getTime(session.getDuration());
        if (currentPlayingSession == position && FTAudioPlayer.getInstance().isPlaying()) {
            playPauseDrawable = R.drawable.recording_pause;
            progressBarVisibility = View.VISIBLE;
            timeColor = R.color.blue;
            timeTextAlpha = 1.0f;
            time = getTime(FTAudioPlayer.getInstance().getCurrentSessionProgress());
        }
        holder.mPlayPauseImageView.setImageResource(playPauseDrawable);
//        holder.mProgressBar.setVisibility(progressBarVisibility);
        holder.mTimeTextView.setText(time);
        holder.mTimeTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), timeColor));
        holder.mTimeTextView.setAlpha(timeTextAlpha);
    }

    private String getTime(long duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(duration);
    }

    public void applyDataChanges(int index) {
        if (index == currentPlayingSession) {
            notifyItemChanged(currentPlayingSession);
        } else {
            currentPlayingSession = index;
            notifyDataSetChanged();
        }
    }

    public interface FTSessionsContainerToAdapterCallback {
        void onResume();

        void onDestroy();

        void play(int currentSession);

        int getCurrentSession();
    }

    class RecordingsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_audio_recordings_name_text_view)
        protected TextView mNameTextView;
        @BindView(R.id.item_audio_recordings_time_text_view)
        protected TextView mTimeTextView;
        @BindView(R.id.item_audio_recordings_play_pause_image_view)
        protected ImageView mPlayPauseImageView;
        @BindView(R.id.item_audio_recordings_play_pause_layout)
        RelativeLayout mPlayPauseLayout;
        @BindView(R.id.item_audio_recordings_progress_bar)
        ProgressBar mProgressBar;
        @BindView(R.id.item_audio_chevron)
        ImageView mChevron;

        RecordingsViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mChevron.setVisibility(View.GONE);
        }

        @OnClick(R.id.item_audio_recordings_play_pause_layout)
        void onPlayPauseTapped() {
            if (currentPlayingSession == getAdapterPosition() && FTAudioPlayer.getInstance().isPlaying()) {
                currentPlayingSession = -1;
                FTAudioPlayer.getInstance().pausePlaying(itemView.getContext(), true);
            } else {
                if (FTAudioPlayer.getInstance().isRecording()) {
                    FTAudioPlayer.showPlayerInProgressAlert(itemView.getContext(), () -> {
                        FTAudioPlayer.getInstance().stopRecording(itemView.getContext(), true);
                        new Handler().postDelayed(() -> onPlayPauseTapped(), 200);
                    });
                    return;
                } else {
                    currentPlayingSession = getAdapterPosition();
                    mContainerCallback.play(getAdapterPosition());
                }
            }

            notifyDataSetChanged();

        }
    }
}
