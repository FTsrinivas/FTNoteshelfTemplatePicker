package com.fluidtouch.noteshelf.templatepicker.models;

import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.store.ui.FTNewNotebookDialog;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;

public class FTTemplatepickerInputInfo {

    String _notebookTitle;
    FTNThemeCategory.FTThemeType _ftThemeType;

    public String get_notebookTitle() {
        return _notebookTitle;
    }

    public void set_notebookTitle(String _notebookTitle) {
        this._notebookTitle = _notebookTitle;
    }

    public FTNThemeCategory.FTThemeType get_ftThemeType() {
        return _ftThemeType;
    }

    public void set_ftThemeType(FTNThemeCategory.FTThemeType _ftThemeType) {
        this._ftThemeType = _ftThemeType;
    }

    public FTTemplateMode get_ftTemplateOpenMode() {
        return _ftTemplateOpenMode;
    }

    public void set_ftTemplateOpenMode(FTTemplateMode _ftTemplateOpenMode) {
        this._ftTemplateOpenMode = _ftTemplateOpenMode;
    }

    public FTBaseShelfActivity get_baseShelfActivity() {
        return _baseShelfActivity;
    }

    public void set_baseShelfActivity(FTBaseShelfActivity _baseShelfActivity) {
        this._baseShelfActivity = _baseShelfActivity;
    }

    FTBaseShelfActivity _baseShelfActivity;

    public FTNewNotebookDialog get_ftNewNotebookDialog() {
        return _ftNewNotebookDialog;
    }

    public void set_ftNewNotebookDialog(FTNewNotebookDialog _ftNewNotebookDialog) {
        this._ftNewNotebookDialog = _ftNewNotebookDialog;
    }

    FTNewNotebookDialog _ftNewNotebookDialog;
    FTTemplateMode _ftTemplateOpenMode;

}
