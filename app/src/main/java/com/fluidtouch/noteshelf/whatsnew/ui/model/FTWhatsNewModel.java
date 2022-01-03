package com.fluidtouch.noteshelf.whatsnew.ui.model;

import androidx.fragment.app.Fragment;


public class FTWhatsNewModel {
    private String slideType;
    private boolean isDataToBeRefreshed;
    private boolean isUserViewed;
    private boolean isUserDismissed;
    private String userViewedTime;
    private Fragment fragmentType;
    public FTWhatsNewModel(String slideType, boolean isDataToBeRefreshed, boolean isUserViewed, boolean isUserDismissed, String userViewedTime, Fragment fragmentType) {
        this.slideType = slideType;
        this.isDataToBeRefreshed = isDataToBeRefreshed;
        this.isUserViewed = isUserViewed;
        this.isUserDismissed = isUserDismissed;
        this.userViewedTime = userViewedTime;
        this.fragmentType = fragmentType;
    }

    public String getSlideType() {
        return slideType;
    }

    public void setSlideType(String slideType) {
        this.slideType = slideType;
    }

    public boolean isDataToBeRefreshed() {
        return isDataToBeRefreshed;
    }

    public void setDataToBeRefreshed(boolean dataToBeRefreshed) {
        isDataToBeRefreshed = dataToBeRefreshed;
    }

    public boolean isUserViewed() {
        return isUserViewed;
    }

    public void setUserViewed(boolean userViewed) {
        isUserViewed = userViewed;
    }

    public boolean isUserDismissed() {
        return isUserDismissed;
    }

    public void setUserDismissed(boolean userDismissed) {
        isUserDismissed = userDismissed;
    }

    public String getUserViewedTime() {
        return userViewedTime;
    }

    public void setUserViewedTime(String userViewedTime) {
        this.userViewedTime = userViewedTime;
    }

    public Fragment getFragmentType() {
        return fragmentType;
    }

    public void setFragmentType(Fragment fragmentType) {
        this.fragmentType = fragmentType;
    }
}
