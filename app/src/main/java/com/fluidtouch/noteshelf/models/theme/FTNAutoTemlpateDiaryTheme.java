package com.fluidtouch.noteshelf.models.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;

import java.util.Date;

public class FTNAutoTemlpateDiaryTheme extends FTNPaperTheme {
    public String templateId = "Modern";
    public Date startDate;
    public Date endDate;

    FTNAutoTemlpateDiaryTheme(NSDictionary metaData) {
        if (metaData != null) {
            if (metaData.containsValue("dynamic_id")) {
                this.dynamicId = (int) metaData.get("dynamic_id").toJavaObject();
            }

            /*if (metaData.containsValue("template_id")) {
                Log.d("TemplatePicker==>"," template_id FTNAutoTemlpateDiaryTheme template_id::-"+metaData.get("template_id").toString());
                this.templateId = metaData.get("template_id").toString();
            } else {
                Log.d("TemplatePicker==>"," template_id FTNAutoTemlpateDiaryTheme Else::-");
            }*/
        }

    }

/*    @Override
    public FTUrl themeTemplateURL() {
        return FTUrl.parse("FTNAutoTemlpateDiaryTheme_template");
    }*/

    /*@Override
    public Bitmap themeThumbnail(Context context) {
        super.themeThumbnail(context);
        return null;
    }*/

}
