package com.fluidtouch.noteshelf.store.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.fluidtouch.noteshelf.store.model.FTDownloadData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FTDownloadedStorePackData {

    public static FTDownloadedStorePackData storePackData;
    Context mContext;
    SharedPreferences downloadPref;
    String prefName = "DownloadedStorePackData";
    String KEY = "storePackData";

    private FTDownloadedStorePackData(Context context) {
        mContext = context;
        downloadPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static FTDownloadedStorePackData getInstance(Context context) {
        if (storePackData == null)
            storePackData = new FTDownloadedStorePackData(context);
        return storePackData;
    }

    public void setData(String key, Object data) {
        SharedPreferences.Editor editor = downloadPref.edit();
        if (data instanceof String)
            editor.putString(key, (String) data).commit();
        else
            editor.putInt(key, (int) data).commit();
    }

    public Map<String, FTDownloadData> getStorePackData() {
        Map<String, FTDownloadData> hashMap = null;
        try {
            Type type = new TypeToken<Map<String, FTDownloadData>>() {
            }.getType();
            hashMap = new Gson().fromJson((String) getData(KEY, ""), type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap != null ? hashMap : new HashMap<String, FTDownloadData>();
    }

    public void setStorePackData(FTDownloadData ftDownLoadData) {
        Map<String, FTDownloadData> oldData = FTDownloadedStorePackData.getInstance(mContext).getStorePackData();
        oldData.put(ftDownLoadData.category, ftDownLoadData);
        setData(KEY, new Gson().toJson(oldData));
    }

    public Object getData(String key, Object defValue) {
        if (defValue instanceof String)
            return downloadPref.getString(key, "");
        else
            return downloadPref.getInt(key, 0);
    }

}
