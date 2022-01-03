package com.fluidtouch.noteshelf.preferences;

/**
 * Created by Sreenu on 20/12/18
 */
public class IndividualDocumentPref extends FTBasePref {
    @Override
    public IndividualDocumentPref init(String prefName) {
        setSharedPreferences(prefName);
        return this;
    }
}
