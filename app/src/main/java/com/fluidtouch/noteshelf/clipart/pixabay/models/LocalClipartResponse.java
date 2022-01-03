package com.fluidtouch.noteshelf.clipart.pixabay.models;

import java.util.ArrayList;
import java.util.List;

public class LocalClipartResponse {
    private List<Clipart> clipartList = new ArrayList<>();

    public List<Clipart> getClipartList() {
        return clipartList;
    }

    public void setClipartList(List<Clipart> clipartList) {
        this.clipartList = clipartList;
    }
}