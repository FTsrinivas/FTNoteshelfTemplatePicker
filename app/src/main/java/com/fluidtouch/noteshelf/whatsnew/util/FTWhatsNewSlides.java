package com.fluidtouch.noteshelf.whatsnew.util;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.whatsnew.ui.fragments.FTWhatsFourthFragment;
import com.fluidtouch.noteshelf.whatsnew.ui.fragments.FTWhatsNewFirstFragment;
import com.fluidtouch.noteshelf.whatsnew.ui.fragments.FTWhatsNewSecondFragment;
import com.fluidtouch.noteshelf.whatsnew.ui.fragments.FTWhatsNewThirdFragment;
import com.fluidtouch.noteshelf.whatsnew.ui.model.FTWhatsNewModel;
import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTWhatsNewSlides {
    private List<FTWhatsNewModel> FTWhatsNewModelsList = new ArrayList<>();
    private static final FTWhatsNewSlides ourInstance = new FTWhatsNewSlides();

    public static FTWhatsNewSlides getInstance() {
        return ourInstance;
    }


    private FTWhatsNewSlides() {

    }

    public void setWhatsNewModelsList(FTWhatsNewSession FTWhatsNewSession) {
        FTWhatsNewModelsList = new ArrayList<>();
        FTWhatsNewModelsList.add(new FTWhatsNewModel(FTWhatsNewFirstFragment.class.getName(), false, FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewFirstFragment.class.getName() + "_isViewed"), FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewFirstFragment.class.getName() + "_isDismissed"), "", new FTWhatsNewFirstFragment()));
        FTWhatsNewModelsList.add(new FTWhatsNewModel(FTWhatsNewSecondFragment.class.getName(), false, FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewSecondFragment.class.getName() + "_isViewed"), FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewSecondFragment.class.getName() + "_isDismissed"), "", new FTWhatsNewSecondFragment()));
        if (!FTApp.isChineseBuild())
            FTWhatsNewModelsList.add(new FTWhatsNewModel(FTWhatsNewThirdFragment.class.getName(), false, FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewThirdFragment.class.getName() + "_isViewed"), FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewThirdFragment.class.getName() + "_isDismissed"), "", new FTWhatsNewThirdFragment()));
        //FTWhatsNewModelsList.add(new FTWhatsNewModel(FTPageRotationFragment.class.getName(), true, FTWhatsNewSession.getSlideViewedDismissedStatus(FTPageRotationFragment.class.getName() + "_isViewed"), FTWhatsNewSession.getSlideViewedDismissedStatus(FTPageRotationFragment.class.getName() + "_isDismissed"), "", FTPageRotationFragment.newInstance()));
        FTWhatsNewModelsList.add(new FTWhatsNewModel(FTWhatsFourthFragment.class.getName(), false, FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsFourthFragment.class.getName() + "_isViewed"), FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsFourthFragment.class.getName() + "_isDismissed"), "", new FTWhatsFourthFragment()));
    }

    public List<FTWhatsNewModel> getSlidesList(String mode, FTWhatsNewSession FTWhatsNewSession) {
        if (mode.equalsIgnoreCase(FTPreviewModes.MANUAL.name())) {
            return FTWhatsNewModelsList;
        } else {
            List<FTWhatsNewModel> automaticList = new ArrayList<>();
            for (int i = 0; i < FTWhatsNewModelsList.size(); i++) {
                if (FTWhatsNewModelsList.get(i).isUserViewed() && (!FTWhatsNewModelsList.get(i).isUserDismissed())) {
                    Date date = new Date(System.currentTimeMillis());
                    long currentTimeInMillis = date.getTime();
                    long viewedTime = FTWhatsNewSession.getUserViewedTime(FTWhatsNewModelsList.get(i).getSlideType() + "_viewedTime");

                    // Todo need to change with actual threshold time. Used two minutes for testing
                    if (((currentTimeInMillis - viewedTime) / 1000) / 60 > 1440) {
                        continue;
                    } else
                        automaticList.add(FTWhatsNewModelsList.get(i));
                } else if (!FTWhatsNewModelsList.get(i).isUserViewed()) {
                    automaticList.add(FTWhatsNewModelsList.get(i));
                }
            }
            return automaticList;
        }

    }

}
