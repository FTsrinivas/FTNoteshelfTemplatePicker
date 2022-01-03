package com.fluidtouch.noteshelf.cloud.backup;

import android.text.TextUtils;

/**
 * Created by Sreenu on 18/02/19
 */
public abstract class FTBackupException extends Exception {

    public abstract FTBackupError handleException();

    public static void shouldLogException(FTBackupException exception) {
        if (!(exception instanceof FTNoInternetException || exception instanceof FTZipFailedException || exception instanceof FTFileNotFoundException
                || exception instanceof FTSocketTimeOutException || exception instanceof FTTokenExpiredException || exception instanceof FTTooManyRequestException)) {
//            FTLog.logCrashException(exception);
        }
    }

    public static class FTFileNotFoundException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.IGNORE_CURRENT_AND_CONTINUE);
        }
    }

    public static class FTZipFailedException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.IGNORE_CURRENT_AND_CONTINUE);
        }
    }

    public static class FTNoInternetException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_DO_NOT_REPORT);
        }
    }

    public static class FTCouldNotFindHostException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_DO_NOT_REPORT);
        }
    }

    public static class FTSocketTimeOutException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_DO_NOT_REPORT);
        }
    }

    public static class FTTokenExpiredException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_REPORT, "Your session expired, please login again.");
        }
    }

    public static class FTBadRequestException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.REPORT_CURRENT_AND_CONTINUE, "Trouble uploading the document; will try again");
        }
    }

    public static class FTForbiddenException extends FTBackupException {

        private String message = "Forbidden";

        public FTForbiddenException(String message) {
            this.message = message;
        }

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.REPORT_CURRENT_AND_CONTINUE, message);
        }
    }

    public static class FTTooManyRequestException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_DO_NOT_REPORT);
        }
    }

    public static class FTBackendException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_REPORT);
        }
    }

    public static class FTUnknownException extends FTBackupException {
        private String message = "Unknown Error";

        public FTUnknownException() {

        }

        public FTUnknownException(String message) {
            this.message = TextUtils.isEmpty(message) ? "Unknown error" : message;
        }

        public FTUnknownException(Exception exception) {
            message = exception == null || TextUtils.isEmpty(exception.getMessage()) ? "Unknown error" : exception.getMessage();
        }

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.REPORT_CURRENT_AND_CONTINUE, this.message);
        }
    }

    public static class FTRetryException extends FTBackupException {
        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.IGNORE_CURRENT_AND_CONTINUE);
        }
    }

    public static class FTRelocationException extends FTBackupException {
        private String message = "";

        public FTRelocationException(String message) {
            this.message = message;
        }

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.REPORT_CURRENT_AND_CONTINUE, message != null ? message : "");
        }
    }

    public static class FTMalformedException extends FTBackupException {
        private String message = "";

        public FTMalformedException(String message) {
            this.message = message;
        }

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.REPORT_CURRENT_AND_CONTINUE, message != null ? message : "");
        }
    }

    public static class FTStorageFullException extends FTBackupException {

        @Override
        public FTBackupError handleException() {
            return new FTBackupError(FTBackupError.ErrorHandlingType.HALT_BACKUP_AND_REPORT, "Your backup storage is full, please clean up some storage to continue backup");
        }
    }

    public static class FTRequestTimeOutException extends FTBackupException {
        @Override
        public FTBackupError handleException() {
            return null;
        }
    }

    public static class FTConflictException extends FTBackupException {
        @Override
        public FTBackupError handleException() {
            return null;
        }
    }
}
