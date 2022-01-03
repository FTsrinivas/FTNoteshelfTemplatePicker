package com.fluidtouch.noteshelf.whatsnew.ui.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

public class FTWhatsNewSession {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;
    public static final String PREF_NAME = "Fluid_Touch";



    //
    public FTWhatsNewSession(Context context) {
        this._context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void storeUserStatusForViewDismissed(String key, boolean value)
    {
        editor.putBoolean(key, value);
        editor.commit();
    }
  public void storeUserSlideViewedTime(String key, Long value)
    {
        editor.putLong(key, value);
        editor.commit();
    }

    public void storeUserSlidesData(String key, String value)
    {
        editor.putString(key, value);
        editor.commit();
    }

    public boolean getSlideViewedDismissedStatus(String key){
        return pref.getBoolean(key,false);
    }

    public long getUserViewedTime(String key){
        return pref.getLong(key,0L);
    }


}
