package com.fluidtouch.noteshelf.pdfexport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.SizeF;

import com.fluidtouch.noteshelf.annotation.FTAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.document.textedit.FTStyledTextBitmapGenerator;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTFontFamily;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.pdfexport.text.Alignment;
import com.fluidtouch.noteshelf.pdfexport.text.Position;
import com.fluidtouch.noteshelf.pdfexport.text.TextFlow;
import com.fluidtouch.noteshelf.pdfexport.text.TextRect;
import com.fluidtouch.noteshelf.pdfexport.text.TextSequenceUtil;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.tom_roush.harmony.awt.AWTColor;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FTPdfTextRenderer {

    Context mContext;
    public static String customFontPath = FTConstants.DOCUMENTS_ROOT_PATH + "/fonts/NotoSansCJKsc-Regular.ttf";

    public FTPdfTextRenderer(Context context) {
        mContext = context;
        configureRequiredFonts(context);
    }

    public void render(FTAnnotation annotation, Canvas canvas) {
        FTTextAnnotationV1 textAnnotation = (FTTextAnnotationV1) annotation;
        RectF boundingRect = textAnnotation.getBoundingRect();
        boundingRect = FTGeometryUtils.integralRect(boundingRect);
        FTStyledText textData = textAnnotation.getTextInputInfo();
        TextPaint paint = new TextPaint();
        paint.setTextSize(textData.getSize() * mContext.getResources().getDisplayMetrics().density);
        //paint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/" + getFullFontFamily(textData) + ".ttf"));
        paint.setTypeface(Typeface.createFromFile(FTConstants.SYSTEM_FONTS_PATH + getFullFontFamily(textData) + ".ttf"));
        paint.setColor(textData.getColor());
        Paint.Align align = Paint.Align.LEFT;
        float left = boundingRect.left;
        int margin = mContext.getResources().getDimensionPixelOffset(R.dimen._10dp);
        switch (textData.getAlignment()) {
            case NSTextAlignmentLeft:
                align = Paint.Align.LEFT;
                left = boundingRect.left;
                break;
            case NSTextAlignmentRight:
                align = Paint.Align.RIGHT;
                left = boundingRect.right - margin - margin;
                break;
            case NSTextAlignmentCenter:
                align = Paint.Align.CENTER;
                left = boundingRect.centerX() - margin;
                break;
        }
        paint.setTextAlign(align);
        TextRect textRect = new TextRect(paint);
        textRect.prepare(
                textData.getPlainText(),
                (int) boundingRect.width(),
                (int) boundingRect.height());
        //textRect.draw(canvas, left + margin, boundingRect.top + margin);
        int width = (int) boundingRect.width() - margin - margin;
        if (width > 0) {
            StaticLayout staticLayout = StaticLayout.Builder
                    .obtain(textData.getPlainText(), 0, textData.getPlainText().length(), paint, width)
                    .build();
            canvas.translate(left + margin, boundingRect.top + margin);
            staticLayout.draw(canvas);
            canvas.translate(-(left + margin), -(boundingRect.top + margin));
        } else {
            FTLog.logCrashException(new Exception("FTPdfTextRenderer " + width + " <0 "));
        }
        paint = null;
        textData = null;
    }

    private String getFullFontFamily(FTStyledText styledText) {
        return styledText.getFontFamily() + "_" + FTFontFamily.getStyleForInt(styledText.getStyle()).toLowerCase();
//        if(styledText.getStyle() == -1){
//            return styledText.getFontFamily();
//        }else{
//            return styledText.getFontFamily() + "-" + getFontStyle(styledText);
//        }
    }

    public void pdfBoxTextRenderer(PDDocument document, FTAnnotation annotation, PDPageContentStream contentStream, PDPage pdPage, FTNoteshelfPage noteshelfPage) {

        FTTextAnnotationV1 textAnnotation = (FTTextAnnotationV1) annotation;
        RectF boundingRect = textAnnotation.getBoundingRect();
        boundingRect = FTGeometryUtils.integralRect(boundingRect);
        FTStyledText textData = textAnnotation.getTextInputInfo();
        float scale = 1;
        try {
            Alignment align = Alignment.Left;
            if (pdPage.getRotation() == 90 || pdPage.getRotation() == 270) {
                SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getHeight(), pdPage.getMediaBox().getWidth()));
                scale = aspectSize.getWidth() / noteshelfPage.getPageRect().width();
                boundingRect = FTGeometryUtils.scaleRect(boundingRect, scale);
            } else {
                SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getWidth(), pdPage.getMediaBox().getHeight()));
                scale = aspectSize.getWidth() / noteshelfPage.getPageRect().width();
                boundingRect = FTGeometryUtils.scaleRect(boundingRect, scale);
            }
            boundingRect = noteshelfPage.getFTPdfDocumentRef().mapPageCoordinateToDevice(noteshelfPage.associatedPageIndex - 1, pdPage.getRotation(), boundingRect);
            int margin = (int) (mContext.getResources().getDimensionPixelOffset(R.dimen._10dp) * scale);
            float left = boundingRect.left + margin;
            float top = boundingRect.top - margin;
            switch (textData.getAlignment()) {
                case NSTextAlignmentLeft:
                    align = Alignment.Left;
                    break;
                case NSTextAlignmentRight:
                    align = Alignment.Right;
                    break;
                case NSTextAlignmentCenter:
                    align = Alignment.Center;
                    break;
            }
            float width = boundingRect.width() - margin * 2;
            TextFlow textFlow = new TextFlow();
            PDType0Font pdType0FontEnglish = PDType0Font.load(document, mContext.getAssets().open("fonts/" + getFullFontFamily(textData) + ".ttf"));
//            InputStream inputStream = new FileInputStream(FTConstants.SYSTEM_FONTS_PATH + getFullFontFamily(textData) + ".ttf");
//            PDType0Font pdType0FontEnglish = PDType0Font.load(document, inputStream);
            try {
                pdType0FontEnglish.encode(textData.getPlainText());
                textFlow.addText(textData.getPlainText(), textData.getSize() * scale * mContext.getResources().getDisplayMetrics().density, pdType0FontEnglish, new AWTColor(textData.getColor()));
            } catch (IllegalArgumentException e) {
                if (FTPdfCreator.pdType0FontAllLanguages == null) {
                    File file = new File(customFontPath);
                    if (file.exists()) {
                        try {
                            FTPdfCreator.pdType0FontAllLanguages = PDType0Font.load(document, new FileInputStream(file));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                textFlow.addText(textData.getPlainText(), textData.getSize() * scale * mContext.getResources().getDisplayMetrics().density, FTPdfCreator.pdType0FontAllLanguages, new AWTColor(textData.getColor()));
            }
            textFlow.setMaxWidth(width);
            float xOffset = TextSequenceUtil.getOffset(textFlow, width, align);
            textFlow.drawText(contentStream, new Position(left + xOffset, top),
                    align, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Bitmap bitmap = new FTStyledTextBitmapGenerator().getBitmap(mContext, boundingRect.width(), Math.abs(boundingRect.height()), textAnnotation.getTextInputInfo(), scale);
            FTAnnotation imageAnnotation = FTAnnotationV1.getImageAnnotation(mContext, noteshelfPage, bitmap);
            imageAnnotation.setBoundingRect(textAnnotation.getBoundingRect());
            new FTPdfImageRenderer().pdfBoxImageRender(imageAnnotation, document, pdPage, contentStream, noteshelfPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void configureRequiredFonts(Context context) {
        try {
            File file = new File(customFontPath);
            if (!file.exists()) {
                AssetsUtil assetManager = new AssetsUtil();
                File zipFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/NotoSansCJKsc_Regular.zip");
                assetManager.copyFile("NotoSansCJKsc_Regular.zip", zipFile);
                ZipUtil.unzip(context, zipFile.getAbsolutePath(), file.getParent(), (File unZipFile, Error error) -> {
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
