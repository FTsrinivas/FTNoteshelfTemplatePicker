package com.fluidtouch.noteshelf.clipart.pixabay.providers;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;
import com.fluidtouch.noteshelf.clipart.pixabay.models.ClipartResponse;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FTPixabayClipartProvider {
    //Web URL = https://pixabay.com/api/?key=12288010-dc965642562959af2a4f30221&q=&image_type=vector&safesearch=true&sort=downloads&page=1&per_page=%2060&pretty=true

    //region Member Variables
    private int currentPage = 0;
    private int totalCount = 0;
    private String prevSearchQuery = "";
    private PageGenerator prevTask;
    private int currentCount = 0;
    private String imageType = "";
    //endregion

    //region Search & Get Cliparts from URL
    public synchronized void getNextPage(final FTPixabayProviderCallback listener) {
        if (prevTask != null && prevTask.getStatus().equals(AsyncTask.Status.RUNNING))
            return;
        if (!FTApp.getInstance().isNetworkAvailable()) {
            listener.onLoadCliparts(null, ClipartError.NETWORK_ERROR);
            return;
        }
        if (prevTask != null) {
            prevTask.cancel(false);
        }
        if (currentCount <= totalCount) {
            String reqUrl = FTApp.getInstance().getApplicationContext().getString(R.string.pixabay_url, prevSearchQuery, imageType, (++currentPage), 100);
            prevTask = new PageGenerator((clipartResponse) -> {
                if (clipartResponse != null) {
                    prevTask = null;
                    FTPixabayClipartProvider.this.totalCount = clipartResponse.getTotal();
                    List<Clipart> clipartList = clipartResponse.getCliparts();
                    clipartList.removeIf(p -> (p.getTags().toLowerCase().contains("corona")));
                    currentCount = currentCount + clipartList.size();
                    listener.onLoadCliparts(clipartList, ClipartError.NONE);
                } else {
                    listener.onLoadCliparts(null, ClipartError.NO_RESULTS);
                }
            });
            prevTask.execute(reqUrl);
        }
    }

    public void searchClipartInLibrary(String searchQuery, String imageType, FTPixabayProviderCallback listener) {
        if (!searchQuery.equals(prevSearchQuery) || TextUtils.isEmpty(searchQuery)) {
            prevSearchQuery = searchQuery;
            this.imageType = imageType.toLowerCase();
            currentPage = 0;
            totalCount = 0;
            currentCount = 0;
            if (prevTask != null) {
                prevTask.cancel(true);
            }
        }
        getNextPage(listener);
    }
    //endregion

    //region Background Page Generator
    private static class PageGenerator extends AsyncTask<String, Void, ClipartResponse> {
        private PageGeneratorCallback listener;

        PageGenerator(PageGeneratorCallback listener) {
            this.listener = listener;
        }

        @Override
        protected ClipartResponse doInBackground(String... strings) {
            HttpURLConnection httpURLConnection = null;
            try {
                //Create and open URL connection
                URL url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                //Read data from server
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                //Convert data to string
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                //Close are resources
                httpURLConnection.disconnect();
                inputStream.close();
                bufferedReader.close();
                //Parse JSON data String to Object class
                String jsonData = stringBuilder.toString();
                return new Gson().fromJson(jsonData, ClipartResponse.class);
            } catch (IOException e) {
                FTLog.error(FTLog.CLIPARTS, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ClipartResponse clipartResponse) {
            if (clipartResponse != null && clipartResponse.getCliparts().isEmpty()) {
                listener.onClipartResponseReady(null);
            } else {
                listener.onClipartResponseReady(clipartResponse);
            }
        }
    }

    interface PageGeneratorCallback {
        void onClipartResponseReady(ClipartResponse clipartResponse);
    }
}
//endregion