package com.fluidtouch.noteshelf.clipart.unsplash.providers;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.unsplash.models.LocalUnsplashResponse;
import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;
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

public class FTLocalUnsplashProvider {
    private static final int MAX_RECENTS = 20;
    private ThinDownloadManager downloadManager = new ThinDownloadManager();

    //region Save Cliparts
    public void saveToRecents(UnsplashPhotoInfo clipart, FTUnsplashProviderCallback listener) {
        LocalUnsplashResponse localClipartResponse = getRecentFromLocal();
        List<UnsplashPhotoInfo> recentCliparts = localClipartResponse.getClipartList();

        for (int i = 0; i < recentCliparts.size(); i++) {
            Log.d("FTouch==>", " getThumbURL::" + recentCliparts.get(i).getThumbURL());
        }

        for (UnsplashPhotoInfo existingClipart : recentCliparts) {
            if (existingClipart.getId().equalsIgnoreCase(clipart.getId())) {
                listener.onClipartDownloaded(getUnsplashImagePath(clipart.getId()));
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
                listener.onClipartDownloaded(getUnsplashImagePath(clipart.getId()));
            } else {
                listener.onClipartDownloaded(null);
            }
        });
    }

    public void saveAllRecentCliparts(List<UnsplashPhotoInfo> cliparts) {
        AsyncTask.execute(() -> {
            LocalUnsplashResponse localClipartResponse = new LocalUnsplashResponse();
            localClipartResponse.setClipartList(cliparts);
            writeRecentToJson(localClipartResponse);
        });
    }
    //endregion

    //region Search in Recent Cliparts
    public void searchInRecentCliparts(String searchQuery, FTUnsplashProviderCallback listener) {
        new SearchClipartTask(listener).execute(searchQuery);
    }

    //region Download Clipart
    private void downloadToLocal(UnsplashPhotoInfo clipart, ClipartDownloadListener listener) {
        Log.d("FTouch==>", " downloadToLocal::" + clipart.getId());
        DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(clipart.getRegularURL()))
                .setDestinationURI(Uri.parse(getUnsplashDir().getPath() + "/" + clipart.getId() + FTConstants.PNG_EXTENSION))
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
    public void searchInRecentUnsplash(String searchQuery, FTUnsplashProviderCallback listener) {
        new SearchClipartTask(listener).execute(searchQuery);
    }

    private static class SearchClipartTask extends AsyncTask<String, Void, List<UnsplashPhotoInfo>> {
        FTUnsplashProviderCallback listener;

        SearchClipartTask(FTUnsplashProviderCallback listener) {
            this.listener = listener;
        }

        @Override
        protected List<UnsplashPhotoInfo> doInBackground(String... strings) {
            String searchKey = strings[0];
            Log.d("FTouch==>", "Recent doInBackground searchKey::" + searchKey);
            List<UnsplashPhotoInfo> cliparts = new ArrayList<>();
            for (UnsplashPhotoInfo clipart : getRecentFromLocal().getClipartList()) {
                Log.d("FTouch==>", "Recent doInBackground searchKey::" + clipart.getId());
                if (clipart.getId().contains(searchKey.toLowerCase())) {
                    Log.d("FTouch==>", "Recent doInBackground IF::");
                    cliparts.add(clipart);
                } else {
                    Log.d("FTouch==>", "Recent doInBackground else::");
                }
            }
            return cliparts;
        }

        @Override
        protected void onPostExecute(List<UnsplashPhotoInfo> cliparts) {
            if (cliparts != null) {
                listener.onLoadCliparts(cliparts, ClipartError.NO_RECENTS);
            } else {
                listener.onLoadCliparts(cliparts, ClipartError.NONE);
            }
        }
    }
    //endregion

    //region Delete Clipart
    public void deleteClipart(UnsplashPhotoInfo clipart) {
        AsyncTask.execute(() -> {
            //Delete .png file of Clipart
            File clipartImageFile = new File(getUnsplashDir().getPath() + "/" + clipart.getId() + FTConstants.PNG_EXTENSION);
            if (clipartImageFile.exists()) {
                clipartImageFile.delete();
            }
            //Remove Clipart object from JSON
            LocalUnsplashResponse localClipartResponse = getRecentFromLocal();
            List<UnsplashPhotoInfo> recentCliparts = localClipartResponse.getClipartList();
            if (recentCliparts != null) {
                List<UnsplashPhotoInfo> updatedRecentCliparts = new ArrayList<>(recentCliparts);
                for (UnsplashPhotoInfo recentClipart : recentCliparts) {
                    if (clipart.getId().equalsIgnoreCase(recentClipart.getId())) {
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
    public String getUnsplashImagePath(String clipartId) {
        return getUnsplashDir().getPath() + "/" + clipartId + FTConstants.PNG_EXTENSION;
    }

    private static LocalUnsplashResponse getRecentFromLocal() {
        LocalUnsplashResponse localUnsplashResponse = null;
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(getRecentUnsplashFilePath()));
            localUnsplashResponse = new Gson().fromJson(jsonReader, LocalUnsplashResponse.class);
            jsonReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (localUnsplashResponse == null) {
            localUnsplashResponse = new LocalUnsplashResponse();
        }
        return localUnsplashResponse;
    }

    private static void writeRecentToJson(LocalUnsplashResponse localUnsplashResponse) {
        try {
            String recentsJson = new Gson().toJson(localUnsplashResponse);
            JsonWriter jsonWriter = new JsonWriter(new FileWriter(getRecentUnsplashFilePath()));
            jsonWriter.jsonValue(recentsJson);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRecentUnsplashFilePath() {
        File recentJson = new File(getUnsplashDir(), "recent.json");
        if (!recentJson.exists()) {
            try {
                recentJson.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recentJson.getPath();
    }

    private static File getUnsplashDir() {
        File clipartDir = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/unsplash");
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