package com.fluidtouch.noteshelf.commons.ui;

import static com.noteshelf.cloud.backup.drive.FTGoogleDriveServiceAccountHandler.REQUEST_CODE_SIGN_IN;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.login.EvernoteLoginFragment;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ColorUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.commons.utils.FTPermissionManager;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileExporter.FTFileExporter;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTUniqueFileName;
import com.fluidtouch.noteshelf.evernotesync.FTENPublishManager;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.store.data.FTCreateCustomCover;
import com.fluidtouch.noteshelf.store.data.FTCreateCustomPaper;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.tom_roush.pdfbox.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class FTBaseActivity extends AppCompatActivity implements
        EvernoteLoginFragment.ResultCallback {

    public static final int REQUEST_CODE_ADD_COVER_THEME = 1111;
    public static final int REQUEST_CODE_ADD_PAPER_THEME = 1112;
    public static final int SAVE_FILE = 108;
    public static boolean isSaveToDeviceSelected = false;
    private BroadcastReceiver mThemeChangeReceiver;
    private EvernoteAuthenticationCallback enAuthListener;
    public Uri exportedFileUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpCrashlytics();
        mThemeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setUpToolbarTheme();
            }
        };
    }

    private void setUpCrashlytics() {
        if (FTApp.getPref().get("userId", "").equals("") || FTApp.getPref().get("userId", "") == null) {
            FTApp.getPref().save("userId", UUID.randomUUID().toString().substring(0, 8));
        }
        FTLog.setCrashUserId(FTApp.getPref().get("userId", ""));
        FTLog.logCrashCustomKey("App Version", getString(R.string.current_version, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));
        FTLog.logCrashCustomKey("OS Version", Build.VERSION.RELEASE);
        FTLog.logCrashCustomKey("Device", Build.MODEL);
        FTLog.logCrashCustomKey("Stylus", "" + FTApp.getPref().isStylusEnabled());
    }

    public void setUpToolbarTheme() {
        String toolbarColor = FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));
        }
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(ColorUtil.changeColorHSB(toolbarColor)));
    }

    protected Intent getShareFilteredIntent(Uri exportFileUri) {
        String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(this, exportFileUri);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, exportFileUri);

        List<Intent> intentList = new ArrayList<>();
        Intent openInChooser = Intent.createChooser(shareIntent, "Share to");
        intentList.add(getSaveToGalleryIntent(exportFileUri));

        Intent saveToIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        saveToIntent.addCategory(Intent.CATEGORY_OPENABLE);
        saveToIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        saveToIntent.setType("*/*");
        saveToIntent.putExtra(Intent.EXTRA_TITLE, FileUriUtils.getFileName(this, exportFileUri));
        saveToIntent.putExtra(Intent.EXTRA_STREAM, exportFileUri);
        saveToIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intentList.add(saveToIntent);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));
        return openInChooser;
    }

    private LabeledIntent getSaveToGalleryIntent(Uri exportFileUri) {
        exportedFileUri = exportFileUri;
        Intent intent = new Intent(this, FTSaveToDeviceActivity.class);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, exportFileUri);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return new LabeledIntent(intent, BuildConfig.APPLICATION_ID,
                "Save to device",
                R.mipmap.save_to_device);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mThemeChangeReceiver, new IntentFilter(getString(R.string.theme)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mThemeChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_SIGN_IN:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        data.putExtra(getString(R.string.intent_is_successful), true);
                        ObservingService.getInstance().postNotification("GDrive-signin", true);
                    } else {
                        data = new Intent();
                        data.putExtra(getString(R.string.intent_is_successful), false);
                        ObservingService.getInstance().postNotification("GDrive-signin", false);
                    }
                    sendBackUpSignInBroadcast(data);
                    break;
                case REQUEST_CODE_ADD_COVER_THEME: {
                   FTCreateCustomCover ftCreateCustomCover = new FTCreateCustomCover(this, data.getData(), getSupportFragmentManager());
                   ftCreateCustomCover.create();
                }
                break;
                case REQUEST_CODE_ADD_PAPER_THEME: {
                    Uri finalData = data.getData();
                    ClipData clipData = data.getClipData();
                    String mimeType = "";

                    if (finalData != null) {
                        mimeType = FTFileManagerUtil.getFileMimeTypeByUri(FTBaseActivity.this, finalData);
                    } else {
                        if (clipData != null) {
                            for (int i = 0, end = clipData.getItemCount(); i < end; i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                if (uri != null) {
                                    finalData = uri;
                                }
                            }
                            mimeType = FTFileManagerUtil.getFileMimeTypeByUri(FTBaseActivity.this, finalData);
                        }
                    }

                    Log.d("TemplatePicker==>"," Custom Theme mimeType::-"+mimeType);
                    if (mimeType.equals(getString(R.string.mime_type_application_pdf)) || mimeType.contains("image")) {
                        FTCreateCustomPaper ftCreateCustomPaper = new FTCreateCustomPaper(this, finalData, getSupportFragmentManager());
                        ftCreateCustomPaper.create();
                    } else {
                        Toast.makeText(FTBaseActivity.this, R.string.this_format_not_supported, Toast.LENGTH_LONG).show();
                    }
                }
                break;
                case FTBaseShelfActivity.PICK_EXPORTER:
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(exportedFileUri);
                        OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                        IOUtils.copy(inputStream, outputStream);
                    } catch (Exception e) {
                        FTLog.error(FTFileExporter.class.getName(), e.getMessage());
                        Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } else if (resultCode == SAVE_FILE || isSaveToDeviceSelected) {
            isSaveToDeviceSelected = false;
            Uri save_uri = null;
            if (data != null)
                save_uri = data.getExtras().getParcelable(Intent.EXTRA_STREAM);
            if (save_uri == null)
                save_uri = exportedFileUri;
            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_FILE)) {
                saveToDevice(save_uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) { //case FTBaseShelfActivity.PICK_EXPORTER:
            case SAVE_FILE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (exportedFileUri != null) {
                        saveToDevice(exportedFileUri);
                    }
                } else {
                    Toast.makeText(this, R.string.gallery_access_error, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
//               Release any UI objects that currently hold memory.
//               The user interface has moved to the background.
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
//               Release any memory that your app doesn't need to run.
//               The device is running low on memory while the app is running.
//               The event raised indicates the severity of the memory-related event.
//               If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
//               begin killing background processes.
//                if (currentDocument != null)
//                    for (int i = 0; i < currentDocument.pages(getContext()).size(); i++) {
//                        currentDocument.pages(getContext()).get(i).unloadContents();
//                    }
                Log.e("RUNNING_CRITICAL", "RUNNING_CRITICAL");
                FTLog.crashlyticsLog("RUNNING_CRITICAL");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
//               Release as much memory as the process can.
//               The app is on the LRU list and the system is running low on memory.
//               The event raised indicates where the app sits within the LRU list.
//               If the event is TRIM_MEMORY_COMPLETE, the process will be one of
//               the first to be terminated.
                break;
            default:
//              Release any non-critical data structures.
//              The app received an unrecognized memory level value
//              from the system. Treat this as a generic low-memory message.
                break;
        }
    }

    public void sendBackUpSignInBroadcast(Intent data) {
        data.setAction(getString(R.string.intent_sign_in_result));
        sendBroadcast(data);
    }

    //region Evernote Authentication Callback
    @Override
    public void onLoginFinished(boolean successful) {
        if (successful) {
            if (!FTENPublishManager.getInstance().isEngineUnderExecution) {
                FTENPublishManager.getInstance().enablePublisher();
            }
            Toast.makeText(this, R.string.signed_into_evernote_successfully, Toast.LENGTH_SHORT).show();
            if (enAuthListener != null) {
                enAuthListener.onAuthenticated(successful);
            }
        } else {
            Toast.makeText(this, R.string.unable_to_authenticate_with_evernote, Toast.LENGTH_SHORT).show();
        }
    }

    public void authenticateEvernoteUser(EvernoteAuthenticationCallback listener) {
        if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(this)) {
            if (EvernoteSession.getInstance().isLoggedIn() && listener != null) {
                listener.onAuthenticated(true);
                Bundle bundle = new Bundle();
                bundle.putBoolean("Using Evernote", true);
                bundle.putString("User ID", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                FTApp.getInstance().getAppAnalytics().logEvent("EverNote", bundle);
            } else {
                this.enAuthListener = listener;
                EvernoteSession.getInstance().authenticate(this);
            }
        } else {
            Toast.makeText(this, R.string.check_your_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    //region Evernote Authentication Callback
    public interface EvernoteAuthenticationCallback {
        void onAuthenticated(boolean successful);
    }

    /*
     * This method is called only for Huawei device.
     * Since for Huawei devices, intent data is coming null, It's a workaround for saving data into device.*/

    public void saveToDevice() {
        if (exportedFileUri != null) {
            saveToDevice(exportedFileUri);
        }
    }

    private void saveToDevice(Uri uri) {
        if (uri != null) {
            new AsyncTask<String, Void, String>() {
                FTSmartDialog smartDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    smartDialog = new FTSmartDialog()
                            .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                            .setMessage(getString(R.string.saving))
                            .show(getSupportFragmentManager());
                }

                @Override
                protected String doInBackground(String... strings) {
                    File saving = FileUriUtils.getFile(FTBaseActivity.this, uri);
                    File savaTo = getExternalFilesDir(null);
                    if (savaTo.exists() || savaTo.mkdir()) {
                        try {
                            String name = FTUniqueFileName.uniqueFileNameFor(saving.getName(), new FTUrl(savaTo.getPath()));
                            savaTo = new File(savaTo.getPath(), name);
                            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r", null);
                            InputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                            FTFileManagerUtil.createFileFromInputStream(inputStream, savaTo.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    return savaTo.getAbsolutePath();
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    smartDialog.dismissAllowingStateLoss();
                    if (null != s)
                        FTDialogFactory.showAlertDialog(FTBaseActivity.this, "", getResources().getString(R.string.saved) + " \n" + s, "", getString(R.string.ok), null);
                }
            }.execute();
        }
    }
//endregion
//endregion

//    protected void requestForReview(Activity activity) {
//        ReviewManager reviewManager = ReviewManagerFactory.create(activity);
//        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
//        request.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                ReviewInfo reviewInfo = task.getResult();
//                Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
//                flow.addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful()) {
//                        Log.i("Review", "Final stage successful");
//                    } else {
//                        Log.i("Review", "Final stage failure");
//                    }
//                });
//            }
//        });
//    }
}