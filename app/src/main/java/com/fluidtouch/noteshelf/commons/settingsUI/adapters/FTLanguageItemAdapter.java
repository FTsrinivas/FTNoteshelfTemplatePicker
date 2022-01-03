package com.fluidtouch.noteshelf.commons.settingsUI.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.settingsUI.viewholders.FTLanguageItemViewHolder;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceStatus;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTRecognitionLangResource;
import com.fluidtouch.noteshelf2.R;

import java.util.Observer;

public class FTLanguageItemAdapter extends BaseRecyclerAdapter<FTRecognitionLangResource, FTLanguageItemViewHolder> implements FTLanguageItemViewHolder.FTLanguageItemClickListener {
    private FTLanguageItemViewHolder prevSelected;
    private FTLanguageItemViewHolder lastClicked;
    private Activity mContext;
    private boolean isConvertToText;

    public FTLanguageItemAdapter(Activity context, boolean isConvertToText) {
        this.mContext = context;
        this.isConvertToText = isConvertToText;
    }

    private Observer languageDownloadObserver = (observable, o) -> {
        mContext.runOnUiThread(() -> {
            String languageCode = "";
            FTLanguageResourceStatus resourceStatus = FTLanguageResourceStatus.NONE;
            if (o instanceof FTRecognitionLangResource) {
                FTRecognitionLangResource langResource = (FTRecognitionLangResource) o;
                languageCode = langResource.getLanguageCode();
                resourceStatus = langResource.getResourceStatus();
            } else if (o instanceof String) {
                languageCode = (String) o;
                if (!languageCode.isEmpty())
                    resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
            }
            if (prevSelected != null) {
                prevSelected.downloadImageView.setVisibility(View.GONE);
                prevSelected.checkImageView.setVisibility(View.GONE);
            }
            if (lastClicked != null && lastClicked.getLayoutPosition() != -1
                    && getItem(lastClicked.getLayoutPosition()).getLanguageCode().equals(languageCode)
                    && resourceStatus == FTLanguageResourceStatus.DOWNLOADED) {
                FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Last selected langCode downloaded = " + languageCode);
                ObservingService.getInstance().postNotification("languageChange", languageCode);
                lastClicked = null;
            }
            notifyDataSetChanged();
        });
    };

    @Override
    public int getItemCount() {
        return isConvertToText ? super.getItemCount() - 1 : super.getItemCount();
    }

    @NonNull
    @Override
    public FTLanguageItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ObservingService.getInstance().addObserver("languageDownloaded", languageDownloadObserver);
        return new FTLanguageItemViewHolder(super.getView(viewGroup, R.layout.item_shelf_language_recycler_view), this);
    }

    @Override
    public void onBindViewHolder(@NonNull FTLanguageItemViewHolder viewHolder, int position) {
        FTRecognitionLangResource languageResource = getItem(viewHolder.getAdapterPosition());
        viewHolder.languageTextView.setText(FTLanguageResourceManager.getNativeDisplayName(viewHolder.languageTextView.getContext(), languageResource.getLanguageCode()));
        if (getItem(position).getLanguageCode().equals(FTLanguageResourceManager.languageCodeNone)) {
            viewHolder.languageNameTextView.setVisibility(View.GONE);
            viewHolder.downloadImageView.setVisibility(View.GONE);
        } else {
            viewHolder.languageNameTextView.setVisibility(View.VISIBLE);
            viewHolder.languageNameTextView.setText(FTLanguageResourceManager.getLocalisedLanguage(viewHolder.languageNameTextView.getContext(), languageResource.getLanguageCode()));
        }
        updateLayout(viewHolder);
    }

    private void updateLayout(FTLanguageItemViewHolder viewHolder) {
        FTRecognitionLangResource language = getItem(viewHolder.getAdapterPosition());

        if (language.getResourceStatus().equals(FTLanguageResourceStatus.DOWNLOADED)) {
            viewHolder.progressBar.hide();
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.downloadImageView.setVisibility(View.GONE);
            viewHolder.checkImageView.setVisibility(View.GONE);

            if (language.getLanguageCode().equals(isConvertToText ? FTApp.getPref().get(SystemPref.CONVERT_TO_TEXT_LANGUAGE, "en_US")
                    : FTLanguageResourceManager.getInstance().getCurrentLanguageCode())) {
                viewHolder.checkImageView.setVisibility(View.VISIBLE);
                prevSelected = viewHolder;
            }
        } else if (language.getResourceStatus().equals(FTLanguageResourceStatus.DOWNLOADING) && !language.getLanguageCode().equals(FTLanguageResourceManager.languageCodeNone)) {
            viewHolder.downloadImageView.setVisibility(View.GONE);
            viewHolder.checkImageView.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.progressBar.show();
        } else {
            viewHolder.progressBar.hide();
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.checkImageView.setVisibility(View.GONE);
            viewHolder.downloadImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLanguageClicked(FTLanguageItemViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (position == -1)
            return;
        lastClicked = viewHolder;
        FTRecognitionLangResource language = getItem(position);
        String[] logTags = viewHolder.itemView.getContext().getResources().getStringArray(R.array.recognition_language_event_log_tags);
        if (logTags != null)
            for (String logTag : logTags) {
                if (logTag.contains(language.getLanguageCode())) {
                    FTFirebaseAnalytics.logEvent(logTag.split(",")[1]);
                    break;
                }
            }

        FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Clicked " + language.getLanguageCode());
        notifyDataSetChanged();
        language.downloadResourceOnDemand();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ObservingService.getInstance().removeObserver("languageDownloaded", languageDownloadObserver);
    }
}