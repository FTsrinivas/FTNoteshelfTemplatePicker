package com.fluidtouch.noteshelf.zoomlayout.Gestures;

public interface FTGestureRecognizerDelegate {
    boolean shouldReceiveTouch();
    void didRecognizedGesture();
}
