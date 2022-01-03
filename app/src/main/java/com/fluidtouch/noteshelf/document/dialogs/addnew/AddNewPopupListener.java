package com.fluidtouch.noteshelf.document.dialogs.addnew;

public interface AddNewPopupListener {
    void addNewPage();

    void addNewPageFromTemplate();

    void addNewPageFromPhoto();

    void importDocument();

    void scanDocument();

    void pickFromCamera();

    void pickFromGallery();

    void addNewAudio();
}
