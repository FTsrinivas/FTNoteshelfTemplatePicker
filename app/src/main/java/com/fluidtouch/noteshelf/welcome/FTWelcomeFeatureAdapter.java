package com.fluidtouch.noteshelf.welcome;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTWelcomeFeatureAdapter extends BaseRecyclerAdapter<FTWelcomeFeature, FTWelcomeFeatureAdapter.WelcomeFeatureViewHolder> {
    @NonNull
    @Override
    public WelcomeFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WelcomeFeatureViewHolder(super.getView(parent, R.layout.item_welcome_feature_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull WelcomeFeatureViewHolder holder, int position) {
        FTWelcomeFeature feature = getItem(position);
        holder.featureImageView.setImageResource(feature.getResourceId());
        holder.featureTitleTextView.setText(feature.getTitle());
    }

    class WelcomeFeatureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.feature_image)
        ImageView featureImageView;
        @BindView(R.id.feature_title)
        TextView featureTitleTextView;

        WelcomeFeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}