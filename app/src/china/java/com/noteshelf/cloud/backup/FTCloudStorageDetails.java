package com.noteshelf.cloud.backup;

import java.io.Serializable;
import java.util.Locale;

public class FTCloudStorageDetails implements Serializable {
    public long totalBytes;
    public long consumedBytes;
    public String username;

    public String getStatus() {
        return getSizeInGB(consumedBytes) + " GB of " + getSizeInGB(totalBytes) + " GB used";
    }

    private String getSizeInGB(long bytes) {
        long gbConversionFactor = 1024 * 1024 * 1024;
        return String.format(Locale.getDefault(),"%.1f",(float) bytes / gbConversionFactor);
    }
}
