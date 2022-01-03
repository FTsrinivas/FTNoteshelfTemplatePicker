package com.fluidtouch.noteshelf.clipart.pixabay.providers;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;
import com.fluidtouch.noteshelf.clipart.pixabay.models.LocalClipartResponse;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTPixabayLocalClipartProvider {
    private static final int MAX_RECENTS = 20;
    private ThinDownloadManager downloadManager = new ThinDownloadManager();

    //region Save Cliparts
    public void saveToRecents(Clipart clipart, FTPixabayProviderCallback listener) {
        LocalClipartResponse localClipartResponse = getRecentFromLocal();
        List<Clipart> recentCliparts = localClipartResponse.getClipartList();
        for (Clipart existingClipart : recentCliparts) {
            if (existingClipart.getId().equals(clipart.getId())) {
                listener.onClipartDownloaded(getClipartImagePath(clipart.getId()));
                return;
            }
        }
        downloadToLocal(clipart, downloaded -> {
            if (downloaded) {
                recentCliparts.add(clipart);
                if (recentCliparts.size() > MAX_RECENTS) {
                    recentCliparts.remove(recentCliparts.size() - 1);
                }
                localClipartResponse.setClipartList(recentCliparts);
                writeRecentToJson(localClipartResponse);
                listener.onClipartDownloaded(getClipartImagePath(clipart.getId()));
            } else {
                listener.onClipartDownloaded(null);
            }
        });
    }

    public void saveAllRecentCliparts(List<Clipart> cliparts) {
        AsyncTask.execute(() -> {
            LocalClipartResponse localClipartResponse = new LocalClipartResponse();
            localClipartResponse.setClipartList(cliparts);
            writeRecentToJson(localClipartResponse);
        });
    }
    //endregion

    //region Download Clipart
    private void downloadToLocal(Clipart clipart, ClipartDownloadListener listener) {
        DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(clipart.getLargeImageURL()))
                .setDestinationURI(Uri.parse(getClipartDir().getPath() + "/" + clipart.getId() + FTConstants.PNG_EXTENSION))
                .setStatusListener(new DownloadStatusListenerV1() {

                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        listener.onClipartDownloadedToLocal(true);
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        listener.onClipartDownloadedToLocal(false);
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                    }
                });
        downloadManager.add(downloadRequest);
    }
    //endregion

    //region Search in Recent Cliparts
    public void searchInRecentCliparts(String searchQuery, FTPixabayProviderCallback listener) {
        new SearchClipartTask(listener).execute(searchQuery);
    }

    private static class SearchClipartTask extends AsyncTask<String, Void, List<Clipart>> {
        FTPixabayProviderCallback listener;

        SearchClipartTask(FTPixabayProviderCallback listener) {
            this.listener = listener;
        }

        @Override
        protected List<Clipart> doInBackground(String... strings) {
            String searchKey = strings[0];
            List<Clipart> cliparts = new ArrayList<>();
            Log.d("FTouch==>", "Recent Clipart doInBackground searchKey::" + searchKey + " strings[0]::" + strings[0]);
            for (Clipart clipart : getRecentFromLocal().getClipartList()) {
                Log.d("FTouch==>", "Recent Clipart doInBackground clipart tags::" + clipart.getTags());
                if (clipart.getTags().contains(searchKey.toLowerCase())) {
                    Log.d("FTouch==>", "Recent Clipart doInBackground clipart tags IF::");
                    cliparts.add(clipart);
                } else {
                    Log.d("FTouch==>", "Recent Clipart doInBackground clipart tags ELSE::");
                }
            }
            return cliparts;
        }

        @Override
        protected void onPostExecute(List<Clipart> cliparts) {
            if (cliparts != null && cliparts.isEmpty()) {
                listener.onLoadCliparts(cliparts, ClipartError.NO_RECENTS);
            } else {
                listener.onLoadCliparts(cliparts, ClipartError.NONE);
            }
        }
    }
    //endregion

    //region Delete Clipart
    public void deleteClipart(Clipart clipart) {
        AsyncTask.execute(() -> {
            //Delete .png file of Clipart
            File clipartImageFile = new File(getClipartDir().getPath() + "/" + clipart.getId() + FTConstants.PNG_EXTENSION);
            if (clipartImageFile.exists()) {
                clipartImageFile.delete();
            }
            //Remove Clipart object from JSON
            LocalClipartResponse localClipartResponse = getRecentFromLocal();
            List<Clipart> recentCliparts = localClipartResponse.getClipartList();
            if (recentCliparts != null) {
                List<Clipart> updatedRecentCliparts = new ArrayList<>(recentCliparts);
                for (Clipart recentClipart : recentCliparts) {
                    if (clipart.getId().intValue() == recentClipart.getId().intValue()) {
                        updatedRecentCliparts.remove(recentClipart);
                    }
                }
                localClipartResponse.setClipartList(updatedRecentCliparts);
                writeRecentToJson(localClipartResponse);
            }
        });
    }
    //endregion

    //region File Operations
    public String getClipartImagePath(int clipartId) {
        return getClipartDir().getPath() + "/" + clipartId + FTConstants.PNG_EXTENSION;
    }

    private static LocalClipartResponse getRecentFromLocal() {
        LocalClipartResponse localClipartResponse = null;
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(getRecentClipartFilePath()));
            localClipartResponse = new Gson().fromJson(jsonReader, LocalClipartResponse.class);
            jsonReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (localClipartResponse == null) {
            localClipartResponse = new LocalClipartResponse();
        }
        return localClipartResponse;
    }

    private static void writeRecentToJson(LocalClipartResponse localClipartResponse) {
        try {
            String recentsJson = new Gson().toJson(localClipartResponse);
            JsonWriter jsonWriter = new JsonWriter(new FileWriter(getRecentClipartFilePath()));
            jsonWriter.jsonValue(recentsJson);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRecentClipartFilePath() {
        File recentJson = new File(getClipartDir(), "recent.json");
        if (!recentJson.exists()) {
            try {
                recentJson.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recentJson.getPath();
    }

    private static File getClipartDir() {
        File clipartDir = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/clipart");
        if (!clipartDir.exists())
            clipartDir.mkdirs();
        return clipartDir;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        downloadManager.cancelAll();
        downloadManager.release();
    }

    //endregion

    private interface ClipartDownloadListener {
        void onClipartDownloadedToLocal(boolean downloaded);
    }
}