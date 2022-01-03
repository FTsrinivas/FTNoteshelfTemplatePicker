package com.fluidtouch.noteshelf.globalsearch.processsors;

import android.content.Context;

import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchCallback;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchType;

/**
 * Created by Vineet on 24/9/2019
 */

public class FTSearchProcessorFactory {
    public static FTSearchProcessor getProcessor(Context context, FTGlobalSearchType type, FTGlobalSearchCallback callback) {
        if (type.equals(FTGlobalSearchType.TITLES))
            return new FTNotebookTitleSearchProcessor(context, callback);
        else if (type.equals(FTGlobalSearchType.CONTENT))
            return new FTNotebookContentSearchProcessor(context, callback);
        return new FTNotebookAllSearchProcessor(context, callback);
    }
}