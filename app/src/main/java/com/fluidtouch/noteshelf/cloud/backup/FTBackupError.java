package com.fluidtouch.noteshelf.cloud.backup;

/**
 * Created by Sreenu on 18/02/19
 */
public class FTBackupError {
    public String message = "";
    public ErrorHandlingType severity;

    public FTBackupError(ErrorHandlingType severity) {
        this.severity = severity;
    }

    public FTBackupError(ErrorHandlingType severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public boolean canProceedToNext() {
        if (severity == ErrorHandlingType.HALT_BACKUP_AND_DO_NOT_REPORT || severity == ErrorHandlingType.HALT_BACKUP_AND_REPORT) {
            return false;
        }

        return true;
    }

    public enum ErrorHandlingType {
        IGNORE_CURRENT_AND_CONTINUE,
        HALT_BACKUP_AND_DO_NOT_REPORT,
        REPORT_CURRENT_AND_CONTINUE,
        HALT_BACKUP_AND_REPORT,
        NO_ERROR
    }
}
