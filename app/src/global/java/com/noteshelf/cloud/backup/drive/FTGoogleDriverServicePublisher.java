package com.noteshelf.cloud.backup.drive;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupCloudTable;
import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupOperations;
import com.fluidtouch.noteshelf.cloud.backup.FTBackupException;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.models.DriveErrorResponse;
import com.fluidtouch.noteshelf2.R;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.noteshelf.cloud.backup.FTServicePublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sreenu on 30/01/19
 */
public class FTGoogleDriverServicePublisher implements FTServicePublisher {
    private com.noteshelf.cloud.backup.drive.FTGoogleDriveCloudHelper cloudHelper;
    private FTServicePublishManager.TaskListener mTaskListener;
    private Context context;

    public Drive authenticateService() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(context);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(googleAccount.getAccount());
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("NoteShelf")
                .build();
    }

    public FTGoogleDriverServicePublisher(Context context) {
        this.context = context;
        cloudHelper = new com.noteshelf.cloud.backup.drive.FTGoogleDriveCloudHelper(this);
    }

    @Override
    public void uploadFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        this.mTaskListener = taskListener;
        FTGoogleDriveBackupItem ftGoogleDriveBackupItem = (FTGoogleDriveBackupItem) backupItem;

        if ((zippedFile.length() / 1024) / 1024 > 230) {
            Exception exception = new Exception(context.getString(R.string.file_size_is_too_large));
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, R.string.file_size_is_too_large, Toast.LENGTH_LONG).show();
            });
            onFailure(ftGoogleDriveBackupItem, exception);
            return;
        }

        String fileName = FTDocumentUtils.getFileName(context, FTUrl.parse(TextUtils.isEmpty(backupItem.getRelativePath()) ? url : backupItem.getRelativePath()));
        String relativePath = TextUtils.isEmpty(backupItem.getRelativePath()) ? url : backupItem.getRelativePath();

        cloudHelper.readFile(ftGoogleDriveBackupItem, fileName, relativePath)
                .addOnSuccessListener(backupItem12 -> {
                    if (backupItem12 == null) {
                        createFile(ftGoogleDriveBackupItem, documentUUID, url, zippedFile);
                    } else {
                        updateFile(ftGoogleDriveBackupItem, relativePath, (ftGoogleDriveBackupItem).getCloudId(), zippedFile, url);
                    }
                })
                .addOnFailureListener(e -> {
                    createFile(ftGoogleDriveBackupItem, documentUUID, url, zippedFile);
                });
    }

    @Override
    public void moveFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        this.mTaskListener = taskListener;
        FTGoogleDriveBackupItem ftGoogleDriveBackupItem = (FTGoogleDriveBackupItem) backupItem;

        if ((zippedFile.length() / 1024) / 1024 > 230) {
            Exception exception = new Exception(context.getString(R.string.file_size_is_too_large));
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, R.string.file_size_is_too_large, Toast.LENGTH_LONG).show();
            });
            onFailure(ftGoogleDriveBackupItem, exception);
            return;
        }

        createFolder(ftGoogleDriveBackupItem, splitPath(url), 0, "", folderId -> {
            moveFile(ftGoogleDriveBackupItem, documentUUID, folderId, url, new FTServicePublishManager.ServiceRequestCallback() {
                @Override
                public void onSuccess() {
                    updateFile(ftGoogleDriveBackupItem, url, ftGoogleDriveBackupItem.getCloudId(), zippedFile, url);
                }

                @Override
                public void onFailure(Exception exception) {
                    uploadFile(backupItem, url, documentUUID, zippedFile, mTaskListener);
                }
            });

        });
    }

    private List<String> splitPath(String url) {
        String[] strings = url.split("/");
        return new ArrayList<>(Arrays.asList(strings));
    }

    private void createFile(FTGoogleDriveBackupItem backupItem, String documentUUID, String url, File localFile) {
        if (cloudHelper != null && localFile != null) {
            createFolder(backupItem, splitPath(url), 0, "",
                    folderId -> cloudHelper.createFile(backupItem, localFile, folderId, documentUUID, url)
                            .addOnSuccessListener(backUpItem -> {
                                new FTGoogleDriveBackupOperations().insertItem((FTGoogleDriveBackupCloudTable) backUpItem);
                                mTaskListener.onFinished((FTBackupItem) backUpItem);
                            })
                            .addOnFailureListener(exception -> {
                                onFailure(backupItem, exception);
                            })
            );
        }
    }

    private void updateFile(FTGoogleDriveBackupItem backupItem, String relativePath, String fieldId, File localUpdatedFile, String url) {
        if (cloudHelper != null && localUpdatedFile != null) {
            cloudHelper.updateFile(backupItem, relativePath, fieldId, localUpdatedFile)
                    .addOnSuccessListener(backUpItem -> {
                        new FTGoogleDriveBackupOperations().updateItem((FTGoogleDriveBackupCloudTable) backUpItem);
                        mTaskListener.onFinished((FTBackupItem) backUpItem);
                    })
                    .addOnFailureListener(exception -> {
                        onFailure(backupItem, exception);
                    });
        }
    }

    private void createFolder(FTGoogleDriveBackupItem backupItem, List<String> pathArray, int index, String folderId, OnSuccessListener<String> onSuccessListener) {
        if (cloudHelper != null && !pathArray.isEmpty() && pathArray.size() > index + 1) {
            String name = pathArray.get(index);
            int i = 0;
            StringBuilder relativePath = new StringBuilder();
            do {
                if (i == index) {
                    relativePath.append(pathArray.get(i));
                } else {
                    relativePath.append(pathArray.get(i)).append("/");
                }
                i++;
            } while (i <= index);

            String extensionRemovedName = removeExtension(name);
            cloudHelper.readFolder(backupItem, extensionRemovedName, relativePath.toString())
                    .addOnSuccessListener(item -> {
                        if (item != null) {
                            createFolder(backupItem, pathArray, index + 1, item.getCloudId(), onSuccessListener);
                        } else {
                            cloudHelper.createFolder(backupItem, extensionRemovedName, folderId, relativePath.toString())
                                    .addOnSuccessListener(backUpItem -> {
                                        createFolder(backupItem, pathArray, index + 1, backUpItem.getCloudId(), onSuccessListener);
                                    })
                                    .addOnFailureListener(e -> {
                                        onFailure(backupItem, e);
                                    });
                        }
                    })
                    .addOnFailureListener(ex -> {
                        onFailure(backupItem, ex);
                    });
        } else {
            onSuccessListener.onSuccess(folderId);
        }
    }

    private String removeExtension(String name) {
        return FTApp.removeExtension(name);
    }

    private void moveFile(FTGoogleDriveBackupItem backupItem, String documentUUID, String toFolderId, String newRelativePath, FTServicePublishManager.ServiceRequestCallback serviceRequestCallback) {
        if (cloudHelper != null) {
            List<FTGoogleDriveBackupItem> list = new ArrayList<>(new FTGoogleDriveBackupOperations().getList(documentUUID));
            if (!list.isEmpty()) {
                cloudHelper.moveFile(backupItem, list.get(0).getCloudId(), toFolderId)
                        .addOnSuccessListener(backUpItem -> {
                            new FTGoogleDriveBackupOperations().updateItem((FTGoogleDriveBackupCloudTable) backUpItem);
                            serviceRequestCallback.onSuccess();
                        })
                        .addOnFailureListener(serviceRequestCallback::onFailure);
            }
        }
    }

    private void onFailure(FTGoogleDriveBackupItem backupItem, Exception exception) {
        FTBackupException backupException = new FTBackupException.FTUnknownException(exception);
        if (exception == null)
            backupException = new FTBackupException.FTUnknownException();
        else if (exception instanceof UnknownHostException) {
            backupException = new FTBackupException.FTCouldNotFindHostException();
        } else if (exception instanceof SocketTimeoutException) {
            backupException = new FTBackupException.FTSocketTimeOutException();
        } else if (exception instanceof GoogleJsonResponseException) {
            try {
                DriveErrorResponse response = new Gson().fromJson(((GoogleJsonResponseException) exception).getContent(), DriveErrorResponse.class);
                int statusCode = response.getCode();
                if (statusCode == 400) {
                    backupException = new FTBackupException.FTBadRequestException();
                } else if (statusCode == 401) {
                    backupException = new FTBackupException.FTTokenExpiredException();
                } else if (statusCode == 403) {
                    String message = response.getMessage();
                    if (message.contains("Properties and app properties are limited to 124 bytes")) {
                        message = context.getString(R.string.character_limit_exceeded);
                    }
                    backupException = new FTBackupException.FTForbiddenException(message);
                } else if (statusCode == 404) {
                    backupException = new FTBackupException.FTFileNotFoundException();
                } else if (statusCode == 429) {
                    backupException = new FTBackupException.FTTooManyRequestException();
                } else if (statusCode == 500) {
                    backupException = new FTBackupException.FTBackendException();
                }
            } catch (Exception e) {
                backupException = new FTBackupException.FTUnknownException(e);
            }
        } else if (exception instanceof GoogleAuthException || exception instanceof GoogleAuthIOException) {
            backupException = new FTBackupException.FTTokenExpiredException();
        } else if (exception instanceof FileNotFoundException) {
            backupException = new FTBackupException.FTFileNotFoundException();
        }

        mTaskListener.onFailure((FTBackupItem) backupItem, backupException);
    }
}
