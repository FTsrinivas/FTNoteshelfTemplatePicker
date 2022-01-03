package com.fluidtouch.noteshelf.shelf.rating;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.RatingPref;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by sreenu on 24/12/20.
 */
public class FTAppRating {
    private FTAppRating() {
        // Do nothing for now
    }

    public static class Builder {
        RatingPref ratingPref;

        public Builder() {
            ratingPref = new RatingPref().init(RatingPref.PREF_NAME);
        }

        public Builder setMinimumLaunchTimes(int minimumLaunchTimes) {
            ratingPref.setMinimumLaunchTimes(minimumLaunchTimes);
            return this;
        }

        public Builder setMinimumDays(int minimumDays) {
            ratingPref.setMinimumDays(minimumDays);
            return this;
        }

        public Builder setMinimumLaunchTimesToShowAgain(int minimumLaunchTimesToShowAgain) {
            ratingPref.setMinimumLaunchTimesToShowAgain(minimumLaunchTimesToShowAgain);
            return this;
        }

        public Builder setMinimumDaysToShowAgain(int minimumDaysToShowAgain) {
            ratingPref.setMinimumDaysToShowAgain(minimumDaysToShowAgain);
            return this;
        }

        public Builder showIfMeetConditions() {
            if (ratingPref.shouldShowDialogForThisVersion() && ratingPref.isNoThanksTapped()) {
                int launchTimesCheck;
                int daysCheck;
                if (ratingPref.wasLaterTapped()) {
                    launchTimesCheck = ratingPref.getMinimumLaunchTimesToShowAgain();
                    daysCheck = ratingPref.getMinimumDaysToShowAgain();
                } else {
                    launchTimesCheck = ratingPref.getMinimumLaunchTimes();
                    daysCheck = ratingPref.getMinimumDays();
                }

                if (ratingPref.getLaunchTimes() >= launchTimesCheck) {
                    if (calculateDays(ratingPref.getRemindTimeStamp()) >= daysCheck) {
                        ratingPref.setLaunchTimes(0);
                        ratingPref.setRemindTimeStamp(System.currentTimeMillis());
                        showDialog();
                    } else {
                        ratingPref.increaseLaunchTimes();
                    }
                } else {
                    ratingPref.increaseLaunchTimes();
                }
            }

            return this;
        }

        private long calculateDays(long timeStamp) {
            return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timeStamp);
        }

        private void showDialog() {
            Context context = FTApp.getInstance().getCurActCtx();

            DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ratingPref.setLastVersionCodeRated(BuildConfig.VERSION_CODE);
                    if (BuildConfig.FLAVOR.contains("samsung")) {
                        ApplicationInfo ai = null;
                        try {
                            ai = context.getPackageManager().getApplicationInfo("com.sec.android.app.samsungapps", PackageManager.GET_META_DATA);
                            int inappReviewVersion = ai.metaData.getInt("com.sec.android.app.samsungapps.review.inappReview", 0);

                            if (inappReviewVersion > 0) {
                                showSamsungInAppStore(context);
                            } else {
                                showSamsungStore();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        return;
                    } else if (BuildConfig.FLAVOR.equals("china")) {
                        showHuaweiAppGallery();
                    }
                    showPlayStore();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    ratingPref.setLaterTapped(true);
                } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                    ratingPref.setNoThanksVersion();
                }
            };

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            alertBuilder.setTitle(context.getString(R.string.store_rating));
            alertBuilder.setIcon(R.mipmap.app_icon_launcher);
            if (BuildConfig.FLAVOR.equals("china")) {
                alertBuilder.setMessage(context.getString(R.string.rating_message_huawei));
            } else if (BuildConfig.FLAVOR.equals("samsung") || BuildConfig.FLAVOR.equals("samsungChinese")) {
                alertBuilder.setMessage(context.getString(R.string.rating_message_samsung));
            } else {
                alertBuilder.setMessage(context.getString(R.string.rating_message));
            }
            alertBuilder.setPositiveButton(context.getString(R.string.rate_now), onClickListener);
            alertBuilder.setNegativeButton(context.getString(R.string.later), onClickListener);
            alertBuilder.setNeutralButton(context.getString(R.string.no_thanks), onClickListener);
            AlertDialog alert = alertBuilder.create();
            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }

        private void showPlayStore() {
            final String GOOGLE_PLAY_WEB_URL = "https://play.google.com/store/apps/details?id=";
            final String GOOGLE_PLAY_IN_APP_URL = "market://details?id=";
            final String packageName = "com.fluidtouch.noteshelf2";
            try {
                Uri uri = Uri.parse(GOOGLE_PLAY_IN_APP_URL + packageName);
                Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW, uri);
                FTApp.getInstance().getCurActCtx().startActivity(googlePlayIntent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse(GOOGLE_PLAY_WEB_URL + packageName);
                Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW, uri);
                FTApp.getInstance().getCurActCtx().startActivity(googlePlayIntent);
            }
        }

        private void showSamsungInAppStore(Context context) {
            Intent intent = new Intent("com.sec.android.app.samsungapps.REQUEST_INAPP_REVIEW_AUTHORITY");
            intent.setPackage("com.sec.android.app.samsungapps");
            intent.putExtra("callerPackage", BuildConfig.APPLICATION_ID); // targetPacakge: your package name
            context.sendBroadcast(intent);
        }

        private void showSamsungStore() {
            Intent intent = new Intent();
            intent.setData(Uri.parse("samsungapps://ProductDetail/com.fluidtouch.noteshelf2"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            FTApp.getInstance().getCurActCtx().startActivity(intent);
        }

        /**
         * For reference: https://appgallery.huawei.com/#/app/C102826543
         */
        private void showHuaweiAppGallery() {
            Intent intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
            intent.setPackage("com.huawei.appmarket");
            intent.putExtra("APP_PACKAGE", "com.fluidtouch.noteshelf2");
            FTApp.getInstance().getCurActCtx().startActivity(intent);
        }
    }
}
