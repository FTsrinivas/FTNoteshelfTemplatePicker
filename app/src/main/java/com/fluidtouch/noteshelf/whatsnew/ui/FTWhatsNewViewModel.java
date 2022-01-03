package com.fluidtouch.noteshelf.whatsnew.ui;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FTWhatsNewViewModel extends ViewModel {
    private MutableLiveData<View> viewMutableLiveData=new MutableLiveData<>();
    public void clickEvents(View view){
        viewMutableLiveData.setValue(view);
    }
    public LiveData<View> getClickEvents(){
        return viewMutableLiveData;
    }

}
