package com.fluidtouch.noteshelf.store.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.store.network.FTDownloadDataService;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTStoreInternalItemViewHolder extends RecyclerView.ViewHolder implements FTDownloadDataService.DownloadDataCallback {

    @BindView(R.id.txtTitle)
    public TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    public TextView txtSubTitle;
    @BindView(R.id.imgBanner)
    public ImageView imageView;
    @BindView(R.id.txtGet)
    public TextView txtGet;
    ProgressBar progressBar;

    public FTStoreInternalItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }

    @Override
    public void onDownloadStart() {
        if (progressBar == null)
            return;
        txtGet.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    @Override
    public void onProgressUpdate(int progress) {
        if (progressBar == null)
            return;
        if (progressBar.getVisibility() == View.GONE) {
            txtGet.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
        }
        progressBar.setProgress(progress);
    }

    @Override
    public void onDownloadFinish(boolean isSuccess) {
        if (progressBar == null)
            return;
        if (isSuccess) {
            progressBar.setVisibility(View.GONE);
            txtGet.setText(R.string.tickmark);
            //txtGet.setEnabled(false);
            txtGet.setVisibility(View.VISIBLE);
            txtGet.setBackgroundResource(R.drawable.store_disable_get_bg);
        } else {
            progressBar.setVisibility(View.GONE);
            txtGet.setEnabled(true);
            txtGet.setVisibility(View.VISIBLE);
        }
    }
}
