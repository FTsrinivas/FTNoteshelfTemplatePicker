package com.fluidtouch.noteshelf.document.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fluidtouch.noteshelf.FTApp;

/**
 * Created by sreenu on 08/12/20.
 */
public class FTDrawerLayout extends DrawerLayout {
    public FTDrawerLayout(@NonNull Context context) {
        super(context);
        setDrawerLockMode();
    }

    public FTDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDrawerLockMode();
    }

    public FTDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDrawerLockMode();
    }

    public void setDrawerLockMode() {
        if (!FTApp.getPref().isQuickAccessPanelEnabled()) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        } else {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
        }

        if (!FTApp.getPref().isFinderEnabled()) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        } else {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
        }
    }
}
