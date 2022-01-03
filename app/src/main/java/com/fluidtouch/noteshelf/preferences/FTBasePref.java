package com.fluidtouch.noteshelf.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.fluidtouch.noteshelf.FTApp;
import com.google.gson.Gson;

/**
 * Created by Sreenu on 23/08/18
 */
public abstract class FTBasePref {
    private SharedPreferences sharedPreferences;

    public abstract FTBasePref init(String prefName);

    void setSharedPreferences(String prefName) {
        sharedPreferences = FTApp.getInstance().getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    /**
     * @param key   Key name of the storing field.
     * @param value Object value of the storing field.
     */
    public void save(String key, Object value) {
        SharedPreferences.Editor editor = getEditor();

        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }

        editor.apply();
    }

    /**
     * @param key          Key name of the storing field.
     * @param defaultValue Default String return value if no value is stored in the required field.
     * @return Object value of the requested field.
     */
    @SuppressWarnings("unchecked")
    public <O> O get(String key, O defaultValue) {
        O returnValue = (O) sharedPreferences.getAll().get(key);
        return returnValue == null ? defaultValue : returnValue;
    }

    public void remove(String key) {
        if (hasKey(key)) {
            getEditor().remove(key).apply();
        }
    }

    public boolean hasKey(String key) {
        return sharedPreferences.contains(key);
    }

    public <GenericClass> GenericClass getSavedObjectFromPreference(String preferenceKey, Class<GenericClass> classType) {
        //SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }
}
