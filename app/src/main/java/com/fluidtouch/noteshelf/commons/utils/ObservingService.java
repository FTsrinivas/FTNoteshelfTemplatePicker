package com.fluidtouch.noteshelf.commons.utils;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class ObservingService {

    private static ObservingService instance;
    HashMap<String, CustomObservable> observables;

    public ObservingService() {
        observables = new HashMap<String, CustomObservable>();
    }

    public synchronized static ObservingService getInstance() {
        if (instance == null) {
            instance = new ObservingService();
        }
        return instance;
    }

    public void addObserver(String notification, Observer observer) {
        CustomObservable observable = observables.get(notification);
        if (observable == null) {
            observable = new CustomObservable();
            observables.put(notification, observable);
        }
        observable.addObserver(observer);
    }

    public void removeObserver(String notification, Observer observer) {
        Observable observable = observables.get(notification);
        if (observable != null) {
            observable.deleteObserver(observer);
        }
    }

    public void postNotification(String notification, Object object) {
        CustomObservable observable = observables.get(notification);
        if (observable != null) {
            observable.notifyObservers(object);
        }
    }

    class CustomObservable extends Observable {
        // To force notifications to be sent
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }
    }
}
