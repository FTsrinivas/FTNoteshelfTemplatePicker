package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.pdfexport.FTPdfTextRenderer;
import com.fluidtouch.noteshelf2.R;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreenu on 24/05/19
 */
public class PdfUtil {
    public static PdfDocument.Page createPage(PdfDocument document, Bitmap bitmap, Bitmap backgroundPdf, Bitmap bitmapMerged, int pageNumber) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(Math.max(bitmap.getWidth(), backgroundPdf == null ? 0 : backgroundPdf.getWidth()),
                Math.max(bitmap.getHeight(), backgroundPdf == null ? 0 : backgroundPdf.getHeight()), pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmapMerged == null ? bitmap : bitmapMerged, 0, 0, null);
        return page;
    }

    private static void addPage(PdfDocument document, Bitmap bitmap, int pageNumber) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
    }

    public static void createPdfFromText(String filePath, String pdfPath, Context context) {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDFont pdfFont = PDType0Font.load(document, context.getAssets().open("fonts/Roboto-Medium.ttf"));
            float fontSize = 16;
            float leading = 1.5f * fontSize;

            PDRectangle mediabox = page.getMediaBox();
            float margin = context.getResources().getDimension(R.dimen.margin_twenty_five);
            float width = mediabox.getWidth() - 2 * margin;
            float startX = mediabox.getLowerLeftX() + margin;
            float startY = mediabox.getUpperRightY() - margin;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            InputStream inputStream = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            float height = 0;
            while ((line = reader.readLine()) != null) {
                try {
                    pdfFont.encode(line);
                    contentStream.setFont(pdfFont, fontSize);
                } catch (IllegalArgumentException e) {
                    FTPdfTextRenderer.configureRequiredFonts(context);
                    File file = new File(FTPdfTextRenderer.customFontPath);
                    if (file.exists()) {
                        try {
                            pdfFont = PDType0Font.load(document, new FileInputStream(file));
                            contentStream.setFont(pdfFont, fontSize);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                List<String> lines = getLines(line, width, fontSize, pdfFont);
                for (String subLine : lines) {
                    if (height > (mediabox.getUpperRightY() - 2 * margin)) {
                        page = new PDPage();
                        document.addPage(page);
                        contentStream.close();
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.setFont(pdfFont, fontSize);
                        contentStream.newLineAtOffset(startX, startY);
                        height = 0;
                    }
                    contentStream.showText(subLine);
                    contentStream.newLineAtOffset(0, -leading);
                    height = height + leading;
                }
            }
            reader.close();
            inputStream.close();
            contentStream.endText();
            contentStream.close();

            document.save(pdfPath);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getLines(String text, float width, float fontSize, PDFont pdfFont) {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = 0;
            try {
                size = fontSize * pdfFont.getStringWidth(subString) / 1000;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (size > width) {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                System.out.printf("'%s' is line\n", subString);
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                System.out.printf("'%s' is line\n", text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

    public static boolean createPdf(List<String> filePaths, String pdfPath, Context context) {
        PdfDocument document = new PdfDocument();
        for (int i = 0; i < filePaths.size(); i++) {
            Bitmap bitmap;
            if (context != null) {
                bitmap = BitmapFactory.decodeFile(filePaths.get(i));
                int rotation = FileUriUtils.getCapturedImageOrientation(context, FileUriUtils.getUriForFile(context, new File(filePaths.get(i))));
                if (rotation > 0) {
                    Matrix mat = new Matrix();
                    mat.postRotate(rotation);
                    try {
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                    } catch (Exception e) {
                        FTLog.crashlyticsLog(filePaths.get(i));
                        FTLog.logCrashException(e);
                        return false;
                    }
                }
            } else {
                bitmap = BitmapFactory.decodeFile(filePaths.get(i));
            }
            if (null != bitmap)
                addPage(document, bitmap, i + 1);
            else
                FTLog.crashlyticsLog("PDFUtil.java Image import bitmap null path" + filePaths.get(i));
        }

        File outputFile = new File(pdfPath);
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        try {
            document.writeTo(new FileOutputStream(outputFile));
        } catch (IOException e) {
            return false;
        }

        // close the document
        document.close();
//        for (int i = 0; i < bitmaps.size(); i++) {
//            bitmaps.get(i).recycle();
//        }
        return true;
    }

    public static boolean createPdf(List<String> filePaths, String pdfPath) {
        return createPdf(filePaths, pdfPath, null);
    }
}
