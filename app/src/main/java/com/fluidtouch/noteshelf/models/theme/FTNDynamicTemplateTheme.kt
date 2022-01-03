package com.fluidtouch.noteshelf.models.theme

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.dd.plist.NSDictionary
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter

class FTNDynamicTemplateTheme(dynamicTemplateInfo: NSDictionary) : FTNPaperTheme() {
    var templateInfoDict: NSDictionary = dynamicTemplateInfo

    init {
        //super.dynamicId = 2
        Log.d("TemplatePicker==>", " dynamic_id status::-" + templateInfoDict.allKeys())
        if (templateInfoDict.containsKey("dynamic_id")) {
            super.dynamicId = Integer.parseInt(templateInfoDict.get("dynamic_id").toString());
        } else {
            Log.d("TemplatePicker==>", " dynamic_id keys::-" + templateInfoDict.keys)
        }
    }

    /*override fun themeTemplateURL(): FTUrl {
        return FTUrl("FTNDynamicTemlpateDiaryTheme_template")
    }*/

    override fun themeThumbnailOnCallBack(mContext: Context, ftnTheme: FTNTheme, callBack: FTTemplateDetailedInfoAdapter,
                                          childViewHolder: FTTemplateDetailedInfoAdapter.ThemeViewHolder ) {
        //Log.d("TemplatePicker==>", " themeThumbnailOnCallBack thumbnailURLPath::-"+ftnTheme.thumbnailURLPath)
        super.themeThumbnailOnCallBack(mContext, ftnTheme, callBack, childViewHolder)
    }

    /*    @Override
    public FTUrl themeTemplateURL() {
        return FTUrl.parse("FTNAutoTemlpateDiaryTheme_template");
    }*/

    /*override fun themeThumbnail(context: Context?): Bitmap? {
        super.themeThumbnail(context)
        return null
    }*/
}