package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

public class SavedData {
    @ColumnInfo(name = "displayName")
    protected String displayName;

    @ColumnInfo(name = "actionType")
    protected String actionType;

    @ColumnInfo(name = "filePath")
    @PrimaryKey
    @NonNull
    protected String filePath;

    @ColumnInfo(name = "timeAdded", defaultValue = "0")
    protected long timeAdded;

    public SavedData() {

    }

    public SavedData(String displayName, @NotNull String filePath) {
        this.displayName = displayName;
        this.filePath = filePath;
    }

    public SavedData(FileData copy) {
        this.displayName = copy.getDisplayName();
        this.filePath = copy.getFilePath();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;

        if (filePath.contains("/")) {
            String temp = filePath;
            while (temp.charAt(temp.length() - 1) == '/') {
                temp = temp.substring(0, filePath.length() - 1);
            }

            if (temp.contains("/")) {
                displayName = filePath.substring(filePath.lastIndexOf("/") + 1);
            } else {
                displayName = "No name";
            }
        } else {
            displayName = "No name";
        }
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
