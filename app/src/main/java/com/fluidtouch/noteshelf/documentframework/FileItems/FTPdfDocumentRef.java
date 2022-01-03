package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;
import android.util.TypedValue;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tom_roush.pdfbox.pdmodel.PDDestinationNameTreeNode;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.PDDocumentNameDestinationDictionary;
import com.tom_roush.pdfbox.pdmodel.PDDocumentNameDictionary;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

import org.benjinus.pdfium.Link;
import org.benjinus.pdfium.PdfPasswordException;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.search.FPDFTextSearchContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class FTPdfDocumentRef implements Parcelable {
    private PdfiumSDK pdfiumSDK;
    private long pdfDocumentPtr;
    private Context mContext;
    private FTUrl fileURL;
    private String password = "";
    private List<String> pdfTexts = new ArrayList<>();
    private boolean isTextCoping = false;
//    PDDocument pdDocument = null;

    public FTPdfDocumentRef(Context context, FTUrl url, String password) {
        super();
        this.fileURL = url;
        this.mContext = context;
        this.password = password;
        this.pageDocumentRef();
    }

    protected FTPdfDocumentRef(Parcel in) {
        pdfiumSDK = in.readParcelable(PdfiumSDK.class.getClassLoader());
        pdfDocumentPtr = in.readLong();
        fileURL = new FTUrl(in.readString());
        password = in.readString();
        pdfTexts = in.createStringArrayList();
        isTextCoping = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(pdfiumSDK, flags);
        dest.writeLong(pdfDocumentPtr);
        dest.writeString(fileURL.getPath());
        dest.writeString(password);
        dest.writeStringList(pdfTexts);
        dest.writeByte((byte) (isTextCoping ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FTPdfDocumentRef> CREATOR = new Creator<FTPdfDocumentRef>() {
        @Override
        public FTPdfDocumentRef createFromParcel(Parcel in) {
            return new FTPdfDocumentRef(in);
        }

        @Override
        public FTPdfDocumentRef[] newArray(int size) {
            return new FTPdfDocumentRef[size];
        }
    };

    public long pageDocumentRef() {
        if (pdfDocumentPtr == 0) {
            pdfiumSDK = new PdfiumSDK(this.mContext);
            try {
                File file = new File(this.fileURL.getPath());
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                pdfDocumentPtr = pdfiumSDK.newDocument(fd, this.password);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            } catch (PdfPasswordException e) {
                e.printStackTrace();
                return 1;
            }
        }

        return pdfDocumentPtr;
    }

    public void copyTextToJson() {
        if (isTextCoping)
            return;
        isTextCoping = true;
        try {
            String rootPath = mContext.getCacheDir() + "/pdftext";
            File outputFile = new File(rootPath + "/" + FTDocumentUtils.getFileNameWithoutExtension(mContext, fileURL) + ".json");
            if (outputFile.exists()) {
                return;
            }

            File rootFile = new File(rootPath);
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }

            JsonArray jsonArray = new JsonArray();

            for (int i = 0; i < pdfiumSDK.getPageCount(pdfDocumentPtr); i++) {
                pdfiumSDK.openPage(pdfDocumentPtr, i);
                RectF rect = new RectF();
                rect.right = pdfiumSDK.getPageWidth(i);
                rect.bottom = pdfiumSDK.getPageHeight(i);

                String s = pdfiumSDK.extractText(pdfDocumentPtr, i, rect);
                pdfTexts.add(s == null ? "" : s);
                jsonArray.add(s);
                pdfiumSDK.closePage(i);
            }

            writeJsonToFile(jsonArray, outputFile);
            isTextCoping = false;
        } catch (Exception e) {
            e.printStackTrace();
            isTextCoping = false;
        }
    }

    private void writeJsonToFile(JsonArray jsonArray, File outputFile) {
        try {
            JsonWriter jsonWriter = new JsonWriter(new FileWriter(outputFile));
            jsonWriter.jsonValue(new Gson().toJson(jsonArray));
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readJsonFromFile(File readFile) {
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(readFile));
            return new Gson().fromJson(jsonReader, new TypeToken<List<String>>() {
            }.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public synchronized Integer pageCount() {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        Integer count = 0;
        if (pdfDocumentPtr != 0) {
            count = pdfiumSDK.getPageCount(pdfDocumentPtr);
        }
        return count;
    }

    public synchronized SizeF getPageSize(Integer index) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        try {
            pdfiumSDK.openPage(this.pdfDocumentPtr, index);
            int width = pdfiumSDK.getPageWidthPoint(index);
            int height = pdfiumSDK.getPageHeightPoint(index);
            if (width == 0) {
                width = 768;
            }
            if (height == 0) {
                height = 960;
            }
            return new SizeF(width, height);
        } catch (Exception e) {
            return new SizeF(768, 960);
        }
    }

    public synchronized RectF
    getPageRectAtIndex(Integer index) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        RectF rect = new RectF();
        try {
            pdfiumSDK.openPage(this.pdfDocumentPtr, index);
            int width = pdfiumSDK.getPageWidthPoint(index);
            int height = pdfiumSDK.getPageHeightPoint(index);
            if (width == 0) {
                width = 768;
            }
            if (height == 0) {
                height = 960;
            }
            SizeF aspectSize = getAspectSizeOfPage(width, height);
            rect.right = aspectSize.getWidth();
            rect.bottom = aspectSize.getHeight();
        } catch (Exception e) {
            SizeF aspectSize = getAspectSizeOfPage(768, 960);
            rect.right = aspectSize.getWidth();
            rect.bottom = aspectSize.getHeight();
        }
        return rect;
    }

    private SizeF getAspectSizeOfPage(int width, int height) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                FTApp.getPref().save("PORTRAIT_ACTION_BAR_HEIGHT", actionBarHeight);
            else
                FTApp.getPref().save("LANDSCAPE_ACTION_BAR_HEIGHT", actionBarHeight);
        }
        int statusBarHeight = ScreenUtil.getStatusBarHeight(mContext);
        int navigationBarHeight = ScreenUtil.getNavigationBarHeight(mContext);
        SizeF aspectSize;
        SizeF screenSize = ScreenUtil.setScreenSize(mContext);
        int screenType = mContext.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (width < height) {
            if (screenType == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenType == Configuration.SCREENLAYOUT_SIZE_SMALL)
                actionBarHeight = FTApp.getPref().get("PORTRAIT_ACTION_BAR_HEIGHT", ScreenUtil.convertDpToPx(mContext, 56));
            aspectSize = FTGeometryUtils.aspectSize(new SizeF(width, height), new SizeF(screenSize.getWidth(), (screenSize.getHeight() - actionBarHeight - statusBarHeight - navigationBarHeight)));
        } else {
            if (screenType == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenType == Configuration.SCREENLAYOUT_SIZE_SMALL)
                actionBarHeight = FTApp.getPref().get("LANDSCAPE_ACTION_BAR_HEIGHT", ScreenUtil.convertDpToPx(mContext, 48));
            aspectSize = FTGeometryUtils.aspectSize(new SizeF(width, height), new SizeF(screenSize.getHeight(), (screenSize.getWidth() - actionBarHeight - statusBarHeight - navigationBarHeight)));
        }
        return aspectSize;
    }

    //Keeping this code to use later for generating thumbnails
    //=======================================
    public synchronized Bitmap pageBackgroundImageOfSize(SizeF size, int pageNumber, String docVersion) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        try {
            pdfiumSDK.openPage(pdfDocumentPtr, pageNumber);
            int width = pdfiumSDK.getPageWidthPoint(pageNumber);
            int height = pdfiumSDK.getPageHeightPoint(pageNumber);

            SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF((float) width, (float) height), size);
            float density = (float) mContext.getResources().getDisplayMetrics().density;
            aspectSize = FTGeometryUtils.scaleSize(aspectSize, density);
            int maxSize = 3072;
            if (aspectSize.getWidth() > maxSize) {
                float tempwidth = maxSize;
                float tempheight = tempwidth * (aspectSize.getHeight() / aspectSize.getWidth());
                aspectSize = new SizeF(tempwidth, tempheight);
            }
            if (aspectSize.getHeight() > maxSize) {
                float tempheight = maxSize;
                float tempwidth = tempheight * (aspectSize.getWidth() /  aspectSize.getHeight());
                aspectSize = new SizeF(tempwidth, tempheight);
            }
            Bitmap bmp = Bitmap.createBitmap((int) aspectSize.getWidth(), (int) aspectSize.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            pdfiumSDK.renderPageBitmap(bmp, pageNumber, 0, 0, bmp.getWidth(), bmp.getHeight(), Float.parseFloat(docVersion) >= 5);
            pdfiumSDK.closePage(pageNumber);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized Bitmap pageBackgroundImage(int pageNumber) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        try {
            pdfiumSDK.openPage(pdfDocumentPtr, pageNumber);
            int width = pdfiumSDK.getPageWidthPoint(pageNumber);
            int height = pdfiumSDK.getPageHeightPoint(pageNumber);

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            pdfiumSDK.renderPageBitmap(bmp, pageNumber, 0, 0, width, height);
            pdfiumSDK.closePage(pageNumber);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap pageBackgroundImage(int pageNumber, Size size) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        try {
            pdfiumSDK.openPage(pdfDocumentPtr, pageNumber);

            Bitmap bmp = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            pdfiumSDK.renderPageBitmap(bmp, pageNumber, 0, 0, bmp.getWidth(), bmp.getHeight(), true);
            pdfiumSDK.closePage(pageNumber);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    //=======================================

    public synchronized List<FTSearchableItem> searchRects(String searchKey, int pageIndex, SizeF pageSize) {
        searchKey = searchKey.trim().toLowerCase();
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        getPdfTexts();
        if (pdfTexts.size() <= 0) {
            copyTextToJson();
            getPdfTexts();
        }
        List<FTSearchableItem> searchableItems = new ArrayList<>();
        if (pdfTexts.size() > 0 && (pdfTexts.get(pageIndex) == null || !pdfTexts.get(pageIndex).toLowerCase().contains(searchKey))) {
            return searchableItems;
        }
        try {
            pdfiumSDK.openPage(pdfDocumentPtr, pageIndex);
            //Searching for matching text and get RectFs based on searchKey
            SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex)), pageSize);
            float scale = aspectSize.getWidth() / pdfiumSDK.getPageWidthPoint(pageIndex);
            FPDFTextSearchContext textSearchContext = (FPDFTextSearchContext) pdfiumSDK.newPageSearch(pdfDocumentPtr, pageIndex, searchKey, false, false);
            RectF textRectF = textSearchContext.searchNext();
            int rotation = pdfiumSDK.getPageRotation(pageIndex);
            do {
                if (textRectF == null) {
                    break;
                }
                textRectF = pdfiumSDK.mapPageCoordinateToDevice(pageIndex, 0, 0, pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex), 0, textRectF);
                textRectF.sort();
                //Add searchItem to page
                FTSearchableItem searchableItem = new FTSearchableItem();
                searchableItem.setRotated(rotation);
                searchableItem.setBoundingRect(FTGeometryUtils.scaleRect(new RectF(textRectF), scale));
                searchableItems.add(searchableItem);
                textRectF = textSearchContext.searchNext();
            } while (textSearchContext.hasNext());
            //Stop search and closing PDF document
            textSearchContext.stopSearch();
            pdfiumSDK.closePage(pageIndex);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchableItems;
    }

    public List<String> getPdfTexts() {
        if (pdfTexts.isEmpty()) {
            File outputFile = new File(mContext.getCacheDir() + "/pdftext" + "/" + FTDocumentUtils.getFileNameWithoutExtension(mContext, fileURL) + ".json");
            if (outputFile.exists()) {
                pdfTexts = readJsonFromFile(outputFile);
            }
        }
        return pdfTexts;
    }

    public synchronized List<Link> getPageLinks(int pageIndex, SizeF pageSize) {
        if (pdfDocumentPtr == 0) {
            this.pageDocumentRef();
        }
        List<Link> links = new ArrayList<>();
        try {
            pdfiumSDK.openPage(pdfDocumentPtr, pageIndex);
            links = pdfiumSDK.getPageLinks(pdfDocumentPtr, pageIndex);
            SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex)), pageSize);
            float scale = aspectSize.getWidth() / pdfiumSDK.getPageWidthPoint(pageIndex);
            for (Link link : links) {
                RectF linkRectF = link.getBounds();
                int rotation = pdfiumSDK.getPageRotation(pageIndex);
                linkRectF = pdfiumSDK.mapPageCoordinateToDevice(pageIndex, 0, 0, pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex), rotation, linkRectF);
                linkRectF.sort();
                link.setBounds(FTGeometryUtils.scaleRect(linkRectF, scale));
            }
            pdfiumSDK.closePage(pageIndex);
//        if (links.size() <= 0) {
//            try {
//                if (pdDocument == null)
//                    pdDocument = PDDocument.load(new FileInputStream(new File(fileURL.getPath())));
//                PDPage pdPage = pdDocument.getPage(pageIndex);
//                List<PDAnnotation> annotations = pdPage.getAnnotations();
//                for (PDAnnotation annot : annotations) {
//                    if (annot instanceof PDAnnotationLink || annot instanceof PDAnnotationWidget) {
//                        PDAction action = null;
//                        PDDestination pDestination = null;
//                        PDRectangle rect = null;
//                        if (annot instanceof PDAnnotationLink) {
//                            PDAnnotationLink link = (PDAnnotationLink) annot;
//                            action = link.getAction();
//                            pDestination = link.getDestination();
//                            rect = link.getRectangle();
//                        } else {
//                            PDAnnotationWidget link = (PDAnnotationWidget) annot;
//                            action = link.getAction();
//                            rect = link.getRectangle();
//                        }
//                        float x = rect.getLowerLeftX();
//                        float y = rect.getUpperRightY();
//                        float width = rect.getWidth();
//                        float height = rect.getHeight();
////                        int rotation = pdPage.getRotation();
////                        if( rotation == 0 )
////                        {
////                            PDRectangle pageSize = pdPage.getMediaBox();
////                            y = pageSize.getHeight() - y;
////                        }
//                        RectF rectF = new RectF(x, y, x + width, y + height);
//                        if (action != null) {
//                            if (action instanceof PDActionURI || action instanceof PDActionGoTo) {
//                                if (action instanceof PDActionURI) {
//                                    // get uri link
//                                    PDActionURI uri = (PDActionURI) action;
//                                    links.add(new Link(rectF, null, uri.getURI()));
//                                } else {
//                                    if (action instanceof PDActionGoTo) {
//                                        // get internal link
//                                        PDDestination destination = ((PDActionGoTo) action).getDestination();
//                                        PDPageDestination pageDestination = null;
//                                        if (destination instanceof PDPageDestination) {
//                                            pageDestination = (PDPageDestination) destination;
//                                        } else {
//                                            if (destination instanceof PDNamedDestination) {
//                                                pageDestination = findNamedDestinationPage(pdDocument.getDocumentCatalog(), (PDNamedDestination) destination);
//                                            }
//                                        }
//                                        if (pageDestination != null)
//                                            links.add(new Link(rectF, pageDestination.retrievePageNumber(), null));
//                                    }
//                                }
//                            }
//                        } else {
//                            if (pDestination != null) {
//                                PDPageDestination pageDestination = null;
//                                if (pDestination instanceof PDPageDestination) {
//                                    pageDestination = (PDPageDestination) pDestination;
//                                } else {
//                                    if (pDestination instanceof PDNamedDestination) {
//                                        pageDestination = findNamedDestinationPage(pdDocument.getDocumentCatalog(), (PDNamedDestination) pDestination);
//                                    }
//                                }
//                                if (pageDestination != null)
//                                    links.add(new Link(rectF, pageDestination.retrievePageNumber(), null));
//                            }
//                        }
//                    }
//                }
//                pdDocument.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        } catch (Exception e) {
            FTLog.logCrashException(e);
        }
        return links;
    }

    public synchronized void closeDocument() {
        if (pdfiumSDK != null) {
            pdfiumSDK.closeDocument(pdfDocumentPtr);
        }
//        if (pdDocument != null) {
//            try {
//                pdDocument.close();
//                pdDocument = null;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        pdfDocumentPtr = 0;
        pdfiumSDK = null;
    }

    public synchronized void closePage(int pageIndex) {
        if (pdfiumSDK != null && pdfiumSDK.hasPage(pageIndex)) {
            pdfiumSDK.closePage(pageIndex);
        }
    }

    public PDPageDestination findNamedDestinationPage(PDDocumentCatalog catalog, PDNamedDestination namedDest) throws IOException {
        PDPageDestination pageDestination = null;
        PDDocumentNameDictionary namesDict = catalog.getNames();
        if (namesDict != null) {
            PDDestinationNameTreeNode destsTree = namesDict.getDests();
            if (destsTree != null) {
                pageDestination = destsTree.getValue(namedDest.getNamedDestination());
            }
        }
        if (pageDestination == null) {
            // Look up /Dests dictionary from catalog
            PDDocumentNameDestinationDictionary nameDestDict = catalog.getDests();
            if (nameDestDict != null) {
                String name = namedDest.getNamedDestination();
                pageDestination = (PDPageDestination) nameDestDict.getDestination(name);
            }
        }
        return pageDestination;
    }

    public PointF covertDeviceCoordinateToPage(int pageIndex, int rotate, int deviceX, int deviceY) {
        if (!pdfiumSDK.hasPage(pageIndex))
            pdfiumSDK.openPage(this.pdfDocumentPtr, pageIndex);
        return pdfiumSDK.mapDeviceCoordinateToPage(pageIndex, 0, 0, pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex), rotate, deviceX, deviceY);
    }

    /**
     * @return mapped coordinates
     */
    public RectF mapPageCoordinateToDevice(int pageIndex, int rotate, RectF rectF) {
        if (!pdfiumSDK.hasPage(pageIndex))
            pdfiumSDK.openPage(this.pdfDocumentPtr, pageIndex);
        return pdfiumSDK.mapDeviceCoordinateToPage(pageIndex, 0, 0, pdfiumSDK.getPageWidthPoint(pageIndex), pdfiumSDK.getPageHeightPoint(pageIndex), rotate, rectF);
    }
}
