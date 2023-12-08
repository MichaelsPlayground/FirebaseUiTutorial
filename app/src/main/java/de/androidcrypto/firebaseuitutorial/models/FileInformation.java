package de.androidcrypto.firebaseuitutorial.models;

import android.net.Uri;

public class FileInformation {
    private String mimeType;
    private String fileName;
    private long fileSize;
    private String fileStorage;
    private String downloadUrlString;
    private long actualTime;
    private String timestamp;

    public FileInformation() {
    }

    public FileInformation(String mimeType, String fileName, Long fileSize) {
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileStorage() {
        return fileStorage;
    }

    public void setFileStorage(String fileStorage) {
        this.fileStorage = fileStorage;
    }

    public String getDownloadUrlString() {
        return downloadUrlString;
    }

    public void setDownloadUrlString(String downloadUrlString) {
        this.downloadUrlString = downloadUrlString;
    }

    public long getActualTime() {
        return actualTime;
    }

    public void setActualTime(long actualTime) {
        this.actualTime = actualTime;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
