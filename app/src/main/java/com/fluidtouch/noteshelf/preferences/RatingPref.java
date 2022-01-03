package com.fluidtouch.noteshelf.preferences;

import com.fluidtouch.noteshelf2.BuildConfig;

/**
 * Created by sreenu on 24/12/20.
 */
public class RatingPref extends FTBasePref {
    public static final String PREF_NAME = "RatingPref";

    @Override
    public RatingPref init(String prefName) {
        setSharedPreferences(prefName);
        return this;
    }

    public boolean shouldShowDialogForThisVersion() {
        return BuildConfig.VERSION_CODE > get(PrefKeys.lastVersionCodeRated, 1);
    }

    public void setLastVersionCodeRated(int lastVersionCodeRated) {
        save(PrefKeys.lastVersionCodeRated, lastVersionCodeRated);
    }

    public int getLaunchTimes() {
        return get(PrefKeys.LAUNCH_TIMES, 0);
    }

    public void setLaunchTimes(int launchTimes) {
        save(PrefKeys.LAUNCH_TIMES, launchTimes);
    }

    public void increaseLaunchTimes() {
        int count = getLaunchTimes();
        save(PrefKeys.LAUNCH_TIMES, count + 1);
    }

    public long getRemindTimeStamp() {
        long remindTimeStamp = get(PrefKeys.remindTimeStamp, -1L);
        if (remindTimeStamp == -1L) {
            remindTimeStamp = System.currentTimeMillis();
            setRemindTimeStamp(remindTimeStamp);
        }

        return remindTimeStamp;
    }

    public void setRemindTimeStamp(long timeStamp) {
        save(PrefKeys.remindTimeStamp, timeStamp);
    }

    public int getMinimumLaunchTimes() {
        return get(PrefKeys.minimumLaunchTimes, 10);
    }

    public void setMinimumLaunchTimes(int minimumLaunchTimes) {
        save(PrefKeys.minimumLaunchTimes, minimumLaunchTimes);
    }

    public int getMinimumDays() {
        return get(PrefKeys.minimumDays, 0);
    }

    public void setMinimumDays(int minimumDays) {
        save(PrefKeys.minimumDays, minimumDays);
    }

    public int getMinimumLaunchTimesToShowAgain() {
        return get(PrefKeys.minimumLaunchTimesToShowAgain, 30);
    }

    public void setMinimumLaunchTimesToShowAgain(int minimumLaunchTimesToShowAgain) {
        save(PrefKeys.minimumLaunchTimesToShowAgain, minimumLaunchTimesToShowAgain);
    }

    public int getMinimumDaysToShowAgain() {
        return get(PrefKeys.minimumDaysToShowAgain, 10);
    }

    public void setMinimumDaysToShowAgain(int minimumDaysToShowAgain) {
        save(PrefKeys.minimumDaysToShowAgain, minimumDaysToShowAgain);
    }

    public Boolean wasLaterTapped() {
        return get(PrefKeys.laterTapped, false);
    }

    public void setLaterTapped(boolean laterTapped) {
        save(PrefKeys.laterTapped, laterTapped);
    }

    private int getNoThanksVersion() {
        return get(PrefKeys.noThanksVersion, 1);
    }

    public void setNoThanksVersion() {
        save(PrefKeys.noThanksVersion, BuildConfig.VERSION_CODE);
    }

    public boolean isNoThanksTapped() {
        return getNoThanksVersion() < BuildConfig.VERSION_CODE;
    }

    interface PrefKeys {
        String lastVersionCodeRated = "lastVersionCodeRated";
        String LAUNCH_TIMES = "launchTimes";
        String remindTimeStamp = "remindTimeStamp";
        String minimumLaunchTimes = "minimumLaunchTimes";
        String minimumDays = "minimumDays";
        String minimumLaunchTimesToShowAgain = "minimumLaunchTimesToShowAgain";
        String minimumDaysToShowAgain = "minimumDaysToShowAgain";
        String laterTapped = "laterTapped";
        String noThanksVersion = "noThanksVersion";
    }
}
