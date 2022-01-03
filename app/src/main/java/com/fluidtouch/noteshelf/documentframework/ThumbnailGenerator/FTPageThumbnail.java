package com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FTPageThumbnail implements PropertyChangeListener {
    public static String strObserver = "onThumbnailUpdate_";
    private FTNoteshelfPage page;
    private Bitmap thumbImage;
    private String documentUUID;
    private String pageUUID;
    private boolean shouldGenerateThumbnail = false;
    private Context context;
    private FTRenderMode ftRenderMode = FTRenderMode.thumbnailGen;
    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable thumbnailTask = new Runnable() {
        @Override
        public void run() {
            requestNewThumbnail();
        }
    };

    public Bitmap getThumbImage() {
        return thumbImage;
    }

    public FTPageThumbnail(FTNoteshelfPage page) {
        super();
        this.page = page;
        this.documentUUID = page.getParentDocument().getDocumentUUID();
        this.pageUUID = page.uuid;
    }

    public boolean shouldGenerateThumbnail() {
        return shouldGenerateThumbnail;
    }

    public void setShouldGenerateThumbnail(boolean shouldGenerateThumbnail) {
        this.shouldGenerateThumbnail = shouldGenerateThumbnail;
    }

    public synchronized void thumbnailImage(final Context context) {
        _thumbnailImage(context);
    }
    //public synchronized void thumbnailImage(final Context context, Bitmap backgroundImage) {

    private void _thumbnailImage(final Context context) {
        this.context = context;
        boolean fileLoaded = (this.thumbImage != null);
        if (this.thumbImage == null || this.thumbImage.isRecycled()) {
            String thumbnailPath = this.thumbnailPath();
            final File thumbnailFile = new File(thumbnailPath);
            Log.d("TemplatePicker==>"," thumbnailFile exists::-"+thumbnailFile.exists());
            if (thumbnailFile.exists()) {
                fileLoaded = true;
                executor.execute(() -> {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    thumbImage = BitmapFactory.decodeFile(thumbnailFile.getPath(), bmOptions);
                    Log.d("TemplatePicker==>"," thumbnailImage thumbImage::-"+thumbImage+" getPath::-"+thumbnailFile.getPath());
                    handler.post(() -> {
                        if (thumbImage != null)
                            ObservingService.getInstance().postNotification(strObserver + pageUUID, new FTThumbnail(pageUUID, thumbImage));
                    });
                });
            }
        }

        long modifiedDate = new File(this.thumbnailPath()).lastModified() / 1000;

        Log.d("TemplatePicker==>"," this.shouldGenerateThumbnail() ::-"+this.shouldGenerateThumbnail() + " page::-"+page);
        if (page != null && !this.shouldGenerateThumbnail()) {
            if (page.lastUpdated > modifiedDate)
                this.setShouldGenerateThumbnail(true);
            else
                this.setShouldGenerateThumbnail(false);
        }

        Log.d("TemplatePicker==>"," this.shouldGenerateThumbnail() ::-"+this.shouldGenerateThumbnail() + " fileLoaded::-"+fileLoaded);
        if (!fileLoaded || this.shouldGenerateThumbnail()) {
            this.setShouldGenerateThumbnail(true);
            //FTThumbnailGenerator.sharedThumbnailGenerator(ftRenderMode).generateThumbnailForPDFPage(context, this.page, backgroundImage);
            FTThumbnailGenerator.sharedThumbnailGenerator(ftRenderMode).generateThumbnailForPDFPage(context, this.page);
        }

        Log.d("TemplatePicker==>"," this.thumbImage ::-"+this.thumbImage + " fileLoaded::-"+fileLoaded);
        if (this.thumbImage != null || !fileLoaded) {
            ObservingService.getInstance().postNotification(strObserver + pageUUID, new FTThumbnail(pageUUID, thumbImage));
        }
    }

    public void updateThumbnailImage(String uuid) {
        handler.post(() -> ObservingService.getInstance().postNotification(strObserver + pageUUID, new FTThumbnail(pageUUID, thumbImage)));
    }

    void updateThumbnail(final Bitmap image, Date updatedDate) {
        if (null != image && !image.isRecycled()) {
            this.thumbImage = image;
            this.setShouldGenerateThumbnail(false);
            final String thumbPath = this.thumbnailPath();
            AsyncTask.execute(() -> {
                if (!image.isRecycled()) {
                    FileOutputStream out;
                    try {
                        out = new FileOutputStream(thumbPath);
                        if (!image.isRecycled())
                            image.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        setShouldGenerateThumbnail(true);
                        thumbnailImage(context);
                    }
                }
            });
        }
    }

    public void delete() {
        File file = new File(this.thumbnailPath());
        if (file.exists()) {
            file.delete();
        }
    }

    private String thumbnailPath() {
        FTUrl thumbnailFolderPath = FTUrl.thumbnailFolderURL();
        FTUrl documentPath = FTUrl.withAppendedPath(thumbnailFolderPath, documentUUID);
        File file = new File(documentPath.getPath());
        if (!file.exists()) {
            file.mkdirs();
        }
        FTUrl thumbnailPath = FTUrl.withAppendedPath(documentPath, pageUUID);
        return thumbnailPath.getPath();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ObservingService.getInstance().postNotification(strObserver + pageUUID, new FTThumbnail(pageUUID, thumbImage));
    }

    public void setPageChanged(Context context) {
        this.context = context;
        handler.removeCallbacks(thumbnailTask);
        handler.post(new Thread(new Runnable() {
            public void run() {
                handler.postDelayed(thumbnailTask, 10000);
            }
        }));

    }

    public void requestNewThumbnail() {
        handler.removeCallbacks(thumbnailTask);
        page.savePage();
        FTThumbnailGenerator.sharedThumbnailGenerator(ftRenderMode).generateThumbnailForPDFPage(context, page);
    }

    public synchronized void removeThumbnail() {
        if (handler != null)
            handler.removeCallbacks(thumbnailTask);
        if (thumbImage != null && !page.getIsinUse()) {
            this.thumbImage = null;
        }
    }


    public interface FTImageGenerationBlock {
        void didFinishWithImage(Bitmap image, String uuidString);
    }

    public class FTThumbnail {
        private String pageUUID = "";
        private Bitmap thumbImage = null;

        FTThumbnail(String pageUUID, Bitmap thumbImage) {
            this.pageUUID = pageUUID;
            this.thumbImage = thumbImage;
        }

        public String getPageUUID() {
            return pageUUID;
        }

        public Bitmap getThumbImage() {
            return thumbImage;
        }
    }
}