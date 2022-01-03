package com.fluidtouch.noteshelf.document;

public class FTLock {
    boolean isInUse = true;
    Object lockObject = new Object();

    public void acquire() {
        synchronized (lockObject) {
            isInUse = true;
        }
    }

    public void waitTillSignal() {
        synchronized (lockObject) {
            while (isInUse) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void signal() {
        synchronized(lockObject){
            isInUse = false;
            lockObject.notifyAll();
        }
    }
}