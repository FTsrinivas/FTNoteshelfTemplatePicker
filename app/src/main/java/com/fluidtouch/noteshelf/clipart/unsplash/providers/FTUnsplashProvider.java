package com.fluidtouch.noteshelf.clipart.unsplash.providers;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;
import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoOwner;
import com.fluidtouch.noteshelf2.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FTUnsplashProvider {
    //region Member Variables
    private int currentPage = 0;
    private static int totalCount = 0;
    private String prevSearchQuery = "";
    private PageGenerator prevTask;
    private int currentCount = 0;
    //endregion

    //region Search & Get Cliparts from URL
    public synchronized void getNextPage(final FTUnsplashProviderCallback listener) {
        if (prevTask != null && prevTask.getStatus().equals(AsyncTask.Status.RUNNING))
            return;
        if (prevTask != null) {
            prevTask.cancel(false);
        }
        if (currentCount <= totalCount) {
            String req_url_unsplash = FTApp.getInstance().getApplicationContext().getString(R.string.unsplash_url, prevSearchQuery, 75, (++currentPage));

            prevTask = new PageGenerator((unSplashPhotosList) -> {
                if (unSplashPhotosList != null) {
                    prevTask = null;
                    List<UnsplashPhotoInfo> unsplashPhotoListList = unSplashPhotosList;
                    currentCount = currentCount + unsplashPhotoListList.size();
                    listener.onLoadCliparts(unsplashPhotoListList, ClipartError.NONE);
                } else {
                    listener.onLoadCliparts(null, ClipartError.NO_RESULTS);
                }
            });
            prevTask.execute(req_url_unsplash);
        }
    }

    public void searchClipartInLibrary(String searchQuery, FTUnsplashProviderCallback listener) {
        if (!searchQuery.equals(prevSearchQuery) || TextUtils.isEmpty(searchQuery)) {
            prevSearchQuery = searchQuery;
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
    private static class PageGenerator extends AsyncTask<String, Void, List<UnsplashPhotoInfo>> {
        private PageGeneratorCallback listener;

        PageGenerator(PageGeneratorCallback listener) {
            this.listener = listener;
        }

        @Override
        protected List<UnsplashPhotoInfo> doInBackground(String... strings) {
            HttpURLConnection httpURLConnection = null;
            try {
                //Create and open URL connection
                URL url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestProperty("Authorization", "Client-ID FO6oGpiGw3ZTAHXAzHiltt611d45PO2FnIdIef0pKX0");
                //Read data from server
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
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
                Log.d("FTouch==>", "jsonData::" + jsonData);
                JSONArray jsonArr;
                JSONObject jsonObj;
                JSONObject dummyjsonObj;
                List<UnsplashPhotoInfo> mUnsplashPhotoListResponse = new ArrayList<>();
                dummyjsonObj = new JSONObject(jsonData);
                if (dummyjsonObj.has("total_pages")) {
                    FTUnsplashProvider.totalCount = dummyjsonObj.getInt("total_pages");
                }
                jsonArr = dummyjsonObj.optJSONArray("results");
                if (jsonArr != null) {
                    for (int i = 0; i < jsonArr.length(); i++) {

                        UnsplashPhotoInfo mUnsplashPhotoInfo = new UnsplashPhotoInfo();
                        UnsplashPhotoOwner mUnsplashPhotoOwner = new UnsplashPhotoOwner();

                        jsonObj = jsonArr.getJSONObject(i);
                        Log.d("FTouch==>", "FTUnsplashProvider jsonObj::" + jsonObj);
                        mUnsplashPhotoInfo.setId(jsonObj.optString("id"));
                        mUnsplashPhotoInfo.setAlt_description(jsonObj.optString("alt_description"));
                        Log.d("FTouch==>", "FTUnsplashProvider jsonObj urls::" + jsonObj.optString("urls"));

                        JSONObject jObject = jsonObj.optJSONObject("urls");
                        Log.d("FTouch==>", "FTUnsplashProvider jsonObj jObject::" + jObject.length());

                        if (jObject.has("raw")) {
                            String rawURL = jObject.optString("raw");
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj YES raw jObject::" + rawURL);
                        } else {
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj No raw jObject::");
                        }

                        if (jObject.has("full")) {
                            String fullURL = jObject.optString("full");
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj YES full jObject::" + fullURL);
                        } else {
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj No full jObject::");
                        }

                        if (jObject.has("regular")) {
                            String regularURL = jObject.optString("regular");
                            mUnsplashPhotoInfo.setRegularURL(regularURL);
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj YES regular jObject::" + regularURL);
                        } else {
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj No regular jObject::");
                        }

                        if (jObject.has("small")) {
                            String smallURL = jObject.optString("small");
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj YES small jObject::" + smallURL);
                        } else {
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj No small jObject::");
                        }

                        if (jObject.has("thumb")) {
                            String thumbURL = jObject.optString("thumb");
                            mUnsplashPhotoInfo.setThumbURL(thumbURL);
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj YES thumb jObject::" + thumbURL);
                        } else {
                            Log.d("FTouch==>", "FTUnsplashProvider jsonObj No thumb jObject::");
                        }

                        JSONObject jPicOwnerObject = jsonObj.optJSONObject("user");

                        mUnsplashPhotoOwner.setId(jPicOwnerObject.optString("id"));
                        Log.d("FTouch==>", "userID::" + jPicOwnerObject.optString("name"));
                        mUnsplashPhotoOwner.setFirst_name(jPicOwnerObject.optString("first_name"));
                        mUnsplashPhotoOwner.setName(jPicOwnerObject.optString("name"));
                        mUnsplashPhotoOwner.setPortfolio_url(jPicOwnerObject.optString("portfolio_url"));

                        JSONObject jPicOwnerLinksObject = jPicOwnerObject.optJSONObject("links");
                        Log.d("FTouch==>", "user html::" + jPicOwnerLinksObject.optString("html"));
                        mUnsplashPhotoOwner.setHtml(jPicOwnerLinksObject.optString("html"));

                        mUnsplashPhotoInfo.setmUnsplashPhotoOwner(mUnsplashPhotoOwner);
                        mUnsplashPhotoListResponse.add(mUnsplashPhotoInfo);
                    }
                }
                return mUnsplashPhotoListResponse;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<UnsplashPhotoInfo> unSplashPhotosList) {
            if (unSplashPhotosList != null && unSplashPhotosList.isEmpty()) {
                unSplashPhotosList = null;
            }
            listener.onUnsplashResponseReady(unSplashPhotosList);
        }

        interface PageGeneratorCallback {
            void onUnsplashResponseReady(List<UnsplashPhotoInfo> unSplashPhotosList);
        }
    }
    //endregion
}