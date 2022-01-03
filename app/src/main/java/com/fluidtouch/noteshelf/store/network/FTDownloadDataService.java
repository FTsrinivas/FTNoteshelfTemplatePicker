package com.fluidtouch.noteshelf.store.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.zendesk.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FTDownloadDataService extends AsyncTask<String, Integer, String> {

    Context mContext;
    String storeItemName = "";
    DownloadDataCallback mCallback;
    private PowerManager.WakeLock mWakeLock;

    public FTDownloadDataService(Context context, DownloadDataCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mCallback.onDownloadStart();
    }

    //region download data from server
    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File file = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            file = new File(ZipUtil.zipFolderPath());
            if (!file.exists()) {
                file.mkdirs();
            }
            storeItemName = sUrl[1];
            Log.d("TemplatePicker==>","Filtering addDownloadsThemeObserver storeItemName::-"+storeItemName);
            file = new File(ZipUtil.zipFolderPath(), storeItemName + ".zip");
            if (!file.exists()) {
                file.createNewFile();
            }
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0)
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
            return file != null ? file.getAbsolutePath() : null;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    //endregion
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mCallback.onProgressUpdate(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        if (result != null) {
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dateToStr = format.format(today);
            Log.d("TemplatePicker==>","templatesCategoryNamesFromDownloads dateToStr::-"+dateToStr + " storeItemName::-"+storeItemName);
            FTApp.getPref().save(storeItemName, dateToStr);
            ZipUtil.unzipDownloadThemes(FTUrl.fromFile(new File(result)), (file, error) -> new MoveFiles().execute(file.getAbsolutePath(), storeItemName));


            //ObservingService.getInstance().postNotification("addDownloadedTheme", storeItemName);

           //templatesCategoryNamesFromDownloads(storeItemName);

        } else {
            mCallback.onDownloadFinish(false);
        }

    }

   /* private TemplatesInfoModel templatesCategoryNamesFromDownloads(TemplatesInfoModel _templatesInfoModel, File themeDir)
    {
        Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads downloadedCategory::-"
                +_templatesInfoModel.get_categoryName());
        for (File f : themeDir.listFiles()) {
            if (f.isDirectory()) {
                FTNTheme theme  = FTNTheme.theme(FTNThemeCategory.getUrl(f.getName()));
                _templatesInfoModel.AddThemesToList(theme);

                Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads downloadedCategory::-"
                        +_templatesInfoModel.get_categoryName()+" ftThemeType:: "+theme.ftThemeType +" packName:: "+theme.packName);
            }
        }

        for (int i=0;i<_templatesInfoModel.get_themeseList().size();i++) {
            Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads downloadedCategory LOG::-"
                    +_templatesInfoModel.get_themeseList().get(i).getCategoryName()+" ftThemeType:: "+_templatesInfoModel.get_themeseList().get(i).packName);
        }

        return _templatesInfoModel;
    }*/

    public interface DownloadDataCallback {

        void onDownloadStart();

        void onProgressUpdate(int progress);

        void onDownloadFinish(boolean isSuccess);
    }

    //region Move unzip files to respected locations
    private class MoveFiles extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            try {
                File themeDir = new File(strings[0]);
                TemplatesInfoModel _templatesInfoModel = new TemplatesInfoModel();
                _templatesInfoModel.set_categoryName(strings[1]);
                for (File f : themeDir.listFiles()) {
                    if (f.isDirectory()) {
                        File rootPath = null;
                        if (FileUtils.getFileExtension(f.getName()).equals("nsp")) {
                            rootPath = new File(FTConstants.DOWNLOADED_PAPERS_PATH2);
                        } else if (FileUtils.getFileExtension(f.getName()).equals("nsc")) {
                            rootPath = new File(FTConstants.DOWNLOADED_COVERS_PATH);
                        }
                        if (rootPath != null) {
                            if (!rootPath.exists())
                                rootPath.mkdirs();
                            File destination = new File(rootPath.getAbsolutePath() + "/" + f.getName());
                            Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads MoveFiles ThemeName::-"+f.getName() + " CategoryNam::-"+strings[1]);
                            if (destination.exists()) {
                                FTFileManagerUtil.deleteRecursive(destination);
                            }
                            f.renameTo(destination);

                        }
                    }
                }
                Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads MoveFiles strings[1]::-"+strings[1]);

                data = strings[1];
                Log.d("TemplatePickerV2","templatesCategoryNamesFromDownloads MoveFiles data::-"+data);

                return data;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                Log.d("TemplatePickerV2","MoveFiles onPostExecute:: "+s);
                ObservingService.getInstance().postNotification("addDownloadedTheme", s);
                ObservingService.getInstance().postNotification("onCoverUpdate", null);
                mCallback.onDownloadFinish(true);
            } else
                mCallback.onDownloadFinish(false);
        }
    }
    //endregion
}

