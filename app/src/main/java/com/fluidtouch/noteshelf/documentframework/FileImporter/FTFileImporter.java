package com.fluidtouch.noteshelf.documentframework.FileImporter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.PdfUtil;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.document.enums.FTCoverOverlayStyle;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FTFileImporter {
    public static final String LOG_TAG = "FTFileImporter";
    private static final String IMPORT_FOLDER_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/Imports/";

    private Context mContext;
    private FileImporterCallbacks callbacks;
    private List<Uri> uris = new ArrayList<>();
    private boolean createNewNotebook;
    private boolean singleNewNotebook;
    private int uriIteratorIndex = 0;
    private boolean isCancelled;
    private FileImportProgressListener progressListener;

    public void startImporting(Context context, Intent intent, boolean createNewNotebook, boolean singleNewNotebook, FileImporterCallbacks callbacks) {
        if (intent == null)
            return;
        this.mContext = context;
        this.callbacks = callbacks;
        this.createNewNotebook = createNewNotebook;
        this.singleNewNotebook = singleNewNotebook;

        FTLog.debug(LOG_TAG, "Started file import process.");

        //Create Imports folder in Temp folder
        File importDir = new File(IMPORT_FOLDER_PATH);
        if (!importDir.exists() && !importDir.mkdirs()) {
            FTLog.debug(LOG_TAG, "Failed to mkdir for Import folder.");
            if (callbacks != null) {
                callbacks.onEachFileImported(null, new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again)));
                callbacks.onAllFilesImported(false);
            }
        } else if (importDir.exists()) {
            FTFileManagerUtil.deleteFilesInsideFolder(importDir);
        }

        //Get Uris from all sources of Intent.
        if (intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
            ClipData clipData = intent.getClipData();
            for (int looper = 0; looper < clipData.getItemCount(); looper++) {
                if (clipData.getItemAt(looper).getUri() != null)
                    uris.add(clipData.getItemAt(looper).getUri());
            }
        } else if (intent.getData() != null) {
            uris.add(intent.getData());
        }
        downloadAndProcess(intent, context);
    }

    public void cancelImporting() {
        isCancelled = true;
        if (callbacks != null)
            callbacks.onAllFilesImported(true);
        callbacks = null;
    }

    private void downloadAndProcess(Intent intent, Context context) {
        if (isCancelled) return;

        if (singleNewNotebook && uriIteratorIndex == uris.size() - 1) {
            singleNewNotebook = false;
        } else if (uriIteratorIndex >= uris.size()) {
            FTLog.debug(LOG_TAG, "Completed file import process.");
            if (callbacks != null) callbacks.onAllFilesImported(false);
            return;
        }
        Uri currentUri = uris.get(uriIteratorIndex);

        //This code is for granting the URI permission
        try {
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, currentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (Exception e) {
            FTLog.error(LOG_TAG, e.getMessage());
        }

        if (currentUri == null) {
            FTLog.error(LOG_TAG, "Uri = null");

            uriIteratorIndex++;
            downloadAndProcess(intent, context);
        } else {
            if (progressListener != null)
                progressListener.onFileImportProgress(uriIteratorIndex + 1, uris.size());
            DownloadFileTask downloadFileTask = new DownloadFileTask(importedFile -> {
                if (importedFile == null || !importedFile.exists()) {
                    FTLog.error(LOG_TAG, "Imported file not found.");
                    if (callbacks != null) {
                        callbacks.onEachFileImported(null, new Error(getContext().getString(R.string.failed_to_import)));
                    }

                    uriIteratorIndex++;
                    downloadAndProcess(intent, context);
                } else {
                    //String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(getContext(), Uri.parse(importedFile.getPath()));
                    String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(getContext(), currentUri);

                    if (TextUtils.isEmpty(mimeType)) {
                        mimeType = FileUriUtils.getMimeType(importedFile);
                        if (TextUtils.isEmpty(mimeType))
                            mimeType = "";
                    }
                    mimeType = mimeType.toLowerCase();

                    //Filter files
                    if (mimeType.contains(getContext().getString(R.string.mime_type_application_pdf))) {
                        processPDFFile(getContext(), importedFile, (documentURL, error) -> {
                            Error importError = null;
                            if (error != null) {
                                FTLog.debug(LOG_TAG, error.getMessage());
                                importError = new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again));
                            }
                            if (callbacks != null)
                                callbacks.onEachFileImported(documentURL, importError);
                            FTFileManagerUtil.deleteRecursive(importedFile);
                        });
                    } else if (mimeType.contains("txt") || mimeType.contains("text")) {
                        processTextFile(getContext(), importedFile, (documentURL, error) -> {
                            Error importError = null;
                            if (error != null) {
                                FTLog.debug(LOG_TAG, error.getMessage());
                                importError = new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again));
                            }
                            if (callbacks != null)
                                callbacks.onEachFileImported(documentURL, importError);
                            FTFileManagerUtil.deleteRecursive(importedFile);
                        });
                    } else if (mimeType.contains(getContext().getString(R.string.mime_type_application_nsa))) {
                        processNSAFile(getContext(), importedFile, (documentURL, error) -> {
                            Error importError = null;
                            if (error != null) {
                                FTLog.debug(LOG_TAG, error.getMessage());
                                importError = new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again));
                            }
                            if (callbacks != null)
                                callbacks.onEachFileImported(documentURL, importError);
                            FTFileManagerUtil.deleteRecursive(importedFile);
                        });
                    } else if (mimeType.contains("image/jpeg") || mimeType.contains("image/jpg") || mimeType.contains("image/png")) {
                        if (singleNewNotebook) {
                            uriIteratorIndex++;
                            downloadAndProcess(intent, context);
                        } else {
                            List<String> imageFilePaths = new ArrayList<>();
                            File importDir = new File(IMPORT_FOLDER_PATH);
                            for (File imageFile : importDir.listFiles()) {
                                if (!imageFile.isDirectory()) {
                                    imageFilePaths.add(imageFile.getPath());
                                }
                            }
                            if (importDir.exists()) {
                                processImageFile(getContext(), imageFilePaths, (documentURL, error) -> {
                                    Error importError = null;
                                    if (error != null) {
                                        FTLog.debug(LOG_TAG, error.getMessage());
                                        importError = new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again));
                                    }
                                    if (callbacks != null)
                                        callbacks.onEachFileImported(documentURL, importError);
                                    FTFileManagerUtil.deleteRecursive(importedFile);
                                });
                            }
                        }
                    } else if (mimeType.contains(getContext().getString(R.string.mime_type_application_all))) {
                        String fileName = FTFileManagerUtil.getFileNameFromUri(getContext(), currentUri);
                        if (fileName.contains(".nsa")) {
                            processNSAFile(getContext(), importedFile, (documentURL, error) -> {
                                Error importError = null;
                                if (error != null) {
                                    FTLog.debug(LOG_TAG, error.getMessage());
                                    importError = new Error(getContext().getString(R.string.unexpected_error_occurred_please_try_again));
                                }
                                if (callbacks != null)
                                    callbacks.onEachFileImported(documentURL, importError);
                                FTFileManagerUtil.deleteRecursive(importedFile);
                            });
                        } else {
                            FTLog.debug(LOG_TAG, "Mime type not supported while importing = " + mimeType);
                            FTLog.crashlyticsLog("Mime type not supported while importing = " + mimeType);
                            if (uris.size() == 1) {
                                if (callbacks != null)
                                    callbacks.onEachFileImported(null, new Error(getContext().getString(R.string.this_format_not_supported)));
                            }
                            uriIteratorIndex++;
                            downloadAndProcess(intent, context);
                        }
                    } else {
                        FTLog.debug(LOG_TAG, "Mime type not supported while importing = " + mimeType);
                        FTLog.crashlyticsLog("Mime type not supported while importing = " + mimeType);
                        if (uris.size() == 1) {
                            if (callbacks != null)
                                callbacks.onEachFileImported(null, new Error(getContext().getString(R.string.this_format_not_supported)));
                        }
                        uriIteratorIndex++;
                        downloadAndProcess(intent, context);
                    }
                }
            });
            downloadFileTask.execute(currentUri);
        }
    }

    private void processPDFFile(Context context, File importedFile, FileProcessCompletionBlock completionBlock) {
        if (!this.createNewNotebook) {
            completionBlock.onFileProcessed(FTUrl.parse(importedFile.getPath()), null);
            return;
        }

        final FTUrl fileUri = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getFileNameWithoutExtension(context, FTUrl.parse(importedFile.getPath())));
        final FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(fileUri);
        FTNCoverTheme coverTheme = new FTNThemeCategory(context, "Transparent", FTNThemeCategory.FTThemeType.COVER).getRandomCoverTheme();

        FTNTheme paperTheme;
        String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
        if (paperPackName.endsWith(".nsp")) {
            paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
        } else {
            paperTheme = new FTNPaperTheme();
            paperTheme.themeName = paperPackName.split("\\.")[0];
            paperTheme.packName = paperPackName;
        }

        FTDocumentInputInfo info = new FTDocumentInputInfo();
        info.inputFileURL = FTUrl.parse(importedFile.getPath());
        info.isTemplate = false;
        info.footerOption = paperTheme.themeFooterOption;
        info.isNewBook = true;
        info.lineHeight = paperTheme.lineHeight;

        if (coverTheme != null) {
            info.overlayStyle = FTCoverOverlayStyle.TRANSPARENT;
            coverTheme.overlayType = 1;
            info.setCoverTheme(coverTheme);
        }

        document.createDocument(context, info, (success, error) -> {
            document.closePdfDocument();
            completionBlock.onFileProcessed(document.getFileURL(), error);
            try {
                FTFileManagerUtil.deleteRecursive(document.getFileURL().getPath());
            } catch (Exception e) {
                // Wait for the moment
            }
        });
    }

    private void processTextFile(Context context, File importedFile, FileProcessCompletionBlock completionBlock) {
        AsyncTask.execute(() -> {
            String pdfPath = FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/" + FTDocumentUtils.getFileNameWithoutExtension(context, FTUrl.parse(importedFile.getPath())) + FTConstants.PDF_EXTENSION;
            PdfUtil.createPdfFromText(importedFile.getPath(), pdfPath, context);
            processPDFFile(context, new File(pdfPath), completionBlock);
        });
    }

    private void processImageFile(Context context, List<String> importedFiles, FileProcessCompletionBlock completionBlock) {
        AsyncTask.execute(() -> {
            String pdfPath = FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/" + getContext().getString(R.string.untitled_notebook) + FTConstants.PDF_EXTENSION;
            PdfUtil.createPdf(importedFiles, pdfPath, context);
            processPDFFile(context, new File(pdfPath), completionBlock);
        });
    }

    private void processNSAFile(Context context, File importedFile, FileProcessCompletionBlock completionBlock) {
        AsyncTask.execute(() -> ZipUtil.unzip(context, importedFile.getAbsolutePath(), (unzippedFile, zipError) -> {
            if (zipError == null && unzippedFile.exists()) {
                FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(FTUrl.parse(unzippedFile.getPath()));
                document.prepareForImporting(context, (success, error) -> completionBlock.onFileProcessed(FTUrl.parse(unzippedFile.getPath()), zipError));
            } else {
                completionBlock.onFileProcessed(null, zipError == null ? new Error("Unzipped imported file does not exist.") : zipError);
            }
        }));
    }

    private interface FileProcessCompletionBlock {
        void onFileProcessed(FTUrl documentURL, Error error);
    }

    private static class DownloadFileTask extends AsyncTask<Uri, Void, File> {
        private FileDownloadCompletionBlock completionBlock;

        interface FileDownloadCompletionBlock {
            void onFileDownloaded(File importedFile);
        }

        DownloadFileTask(FileDownloadCompletionBlock completionBlock) {
            this.completionBlock = completionBlock;
        }

        @Override
        protected File doInBackground(Uri... uris) {
            Uri uri = uris[0];
            String fileName = null;
            InputStream inputStream = null;
            try {
                Context context = FTApp.getInstance().getApplicationContext();
                fileName = FTFileManagerUtil.getFileNameFromUri(context, uri);
                if (TextUtils.isEmpty(fileName)) {
                    fileName = context.getString(R.string.untitled);
                } else if (fileName.contains("#")) {
                    fileName = fileName.replaceAll("#", "");
                } else if (fileName.contains("%")) {
                    fileName = fileName.replaceAll("%", "");
                }
                if (TextUtils.isEmpty(fileName)) {
                    fileName = context.getString(R.string.untitled);
                }

                inputStream = context.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                FTLog.error(LOG_TAG, e.getMessage());
            }
            if (fileName == null || inputStream == null) {
                FTLog.error(LOG_TAG, "InputStream = null or fileName = null.");
            } else {
                return FTFileManagerUtil.createFileFromInputStream(inputStream, IMPORT_FOLDER_PATH.concat(fileName));
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            completionBlock.onFileDownloaded(file);
        }

        @Override
        protected void onCancelled(File file) {
            super.onCancelled(file);
            completionBlock.onFileDownloaded(file);
        }
    }

    public void onUIUpdated() {
        uriIteratorIndex++;
        downloadAndProcess(null, null);
    }

    public void setProgressListener(FileImportProgressListener listener) {
        progressListener = listener;
    }

    public interface FileImportProgressListener {
        void onFileImportProgress(int progress, int total);
    }

    private Context getContext() {
        return mContext == null ? FTApp.getInstance().getApplicationContext() : mContext;
    }

    public interface FileImporterCallbacks {
        void onEachFileImported(FTUrl importedFileUrl, Error error);

        void onAllFilesImported(boolean isCancelled);
    }
}