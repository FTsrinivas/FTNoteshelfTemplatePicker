package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.store.adapter.FTStorePreviewAdapter;
import com.fluidtouch.noteshelf.store.model.FTStoreDialogRespose;
import com.fluidtouch.noteshelf.store.model.FTStorePackItem;
import com.fluidtouch.noteshelf.store.network.FTDownloadDataService;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTStoreDialog extends DialogFragment implements FTDownloadDataService.DownloadDataCallback {

    public FTStorePackItem ftStorePackItem;
    //region Member Variables
    @BindView(R.id.imgStoreDialogClose)
    ImageView close;
    @BindView(R.id.progress_indicator)
    ProgressBar progressBar;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.storeGrid)
    RecyclerView gridView;
    @BindView(R.id.btnDownload)
    Button btnDownload;
    FTStoreCallbacks callback;
    ObservingService mDownloadStatusObserver;
    //endregion

    //region start dialog
    public static FTStoreDialog newInstance(FTStorePackItem ftStorePackItem, FTStoreCallbacks callback, ObservingService downloadStatusObserver) {
        FTStoreDialog ftStoreDialog = new FTStoreDialog();
        ftStoreDialog.ftStorePackItem = ftStorePackItem;
        ftStoreDialog.callback = callback;
        ftStoreDialog.mDownloadStatusObserver = downloadStatusObserver;
        return ftStoreDialog;
    }
    //endregion

    //region Lifecycle Events
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_store_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (ftStorePackItem == null)
            dismiss();
        else {
            txtTitle.setText(ftStorePackItem.getName());
            String languageCode = Locale.getDefault().getLanguage();
            if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
                languageCode = "zh-Hans";
            } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
                languageCode = "zh-Hant";
            }
            Log.d("TemplatePicker==>","FTStoreDialog ftStorePackItem::-"+ftStorePackItem.getMetaData(languageCode)+" languageCode::-"+languageCode);
            new DownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ftStorePackItem.getMetaData(languageCode));
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            if (ftStorePackItem.isDownloaded()) {
                btnDownload.setText(R.string.download_complete);
                //btnDownload.setEnabled(false);
                btnDownload.setBackgroundColor(Color.GRAY);
            }
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onDownloadButtonClick(ftStorePackItem);
                }
            });
            mDownloadStatusObserver.addObserver(ftStorePackItem.getName(), new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    if (arg instanceof String)
                        onDownloadStart();
                    else if (arg instanceof Integer)
                        onProgressUpdate((int) arg);
                    else if (arg instanceof Boolean)
                        onDownloadFinish((boolean) arg);
                }
            });
        }
    }
    //endregion

    //region download callbacks
    @Override
    public void onDownloadStart() {
        btnDownload.setEnabled(false);
        btnDownload.setBackgroundResource(R.drawable.store_download_bg);
        btnDownload.setText(R.string.downloading);
    }
    //endregion

    @Override
    public void onProgressUpdate(int progress) {
        if (isVisible())
            btnDownload.setText(R.string.downloading);
    }

    @Override
    public void onDownloadFinish(boolean isSuccess) {
        if (isSuccess) {
            btnDownload.setEnabled(false);
            btnDownload.setBackgroundColor(Color.GRAY);
            btnDownload.setText(R.string.download_complete);
        } else {
            btnDownload.setEnabled(true);
            btnDownload.setText(R.string.download);
        }
    }

    //region download preview data
    private class DownloadTask extends AsyncTask<String, Void, String> {
        Gson gson = new Gson();

        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            try {
                URL u = new URL(strings[0]);
                Log.d("TemplatePicker==>","FreeDownloads FTStoreDialog doInBackground::-"+u);
                DataInputStream stream = new DataInputStream(u.openStream());
                HashMap<String, Object> pListData = (HashMap<String, Object>) PropertyListParser.parse(stream).toJavaObject();
                data = gson.toJson(pListData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            progressBar.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(data)) {
                FTStoreDialogRespose ftSDResponse = gson.fromJson(data, FTStoreDialogRespose.class);
                ftStorePackItem.setDialogDescription(ftSDResponse.getPack_info());
                ftStorePackItem.setPreviewTokens(ftSDResponse.getPreview_tokens());
                ftSDResponse.setUpdates_info(ftSDResponse.getUpdates_info());
                FTStorePreviewAdapter previewAdapter = new FTStorePreviewAdapter(getContext(), ftStorePackItem);
                Log.d("TemplatePicker==>","FreeDownloads FTStoreDialog onPostExecute::-");
                previewAdapter.add("header");
                previewAdapter.addAll(ftSDResponse.getPreview_items());
                GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
                gridView.setLayoutManager(layoutManager);
                gridView.setAdapter(previewAdapter);
                gridView.addItemDecoration(new EqualSpacingItemDecoration(20));
                gridView.setVisibility(View.VISIBLE);
                btnDownload.setVisibility(View.VISIBLE);
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        switch (position) {
                            case 0:
                                return 3;
                            default:
                                return 1;
                        }
                    }
                });
            }
        }
    }
    //endregion

}
