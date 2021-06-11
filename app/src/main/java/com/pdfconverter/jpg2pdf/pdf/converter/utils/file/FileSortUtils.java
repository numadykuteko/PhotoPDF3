package com.pdfconverter.jpg2pdf.pdf.converter.utils.file;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils.*;

public class FileSortUtils {

    // Sorting order constants

    private FileSortUtils() {}

    public static void performSortOperation(int option, List<FileData> pdf) {
        switch (option) {
            case SORT_BY_DATE_ASC:
                sortFilesByDateOldestToNewest(pdf);
                break;
            case SORT_BY_DATE_DESC:
                sortFilesByDateNewestToOldest(pdf);
                break;
            case SORT_BY_NAME_ASC:
                sortByNameAlphabetical(pdf);
                break;
            case SORT_BY_NAME_DESC:
                sortByNameAlphabeticalRevert(pdf);
                break;
            case SORT_BY_SIZE_ASC:
                sortFilesBySizeIncreasingOrder(pdf);
                break;
            case SORT_BY_SIZE_DESC:
                sortFilesBySizeDecreasingOrder(pdf);
                break;
            case SORT_BY_TYPE:
                sortFilesByTypeOrder(pdf);
                break;
        }
    }

    // SORTING FUNCTIONS

    private static void sortByNameAlphabetical(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> file.getDisplayName().compareToIgnoreCase(file2.getDisplayName()));
    }

    private static void sortByNameAlphabeticalRevert(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> file2.getDisplayName().compareToIgnoreCase(file.getDisplayName()));
    }

    private static void sortFilesByDateOldestToNewest(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> Long.compare(file.getTimeAdded(), file2.getTimeAdded()));
    }

    private static void sortFilesByDateNewestToOldest(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> Long.compare(file2.getTimeAdded(), file.getTimeAdded()));
    }

    private static void sortFilesBySizeIncreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file1.getSize(), file2.getSize()));
    }

    private static void sortFilesBySizeDecreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file2.getSize(), file1.getSize()));
    }

    private static void sortFilesByTypeOrder(List<FileData> filesList) {
        Comparator<FileData> sortByType = (file1, file2) -> {
            int compareType = file1.getFileType().compareToIgnoreCase(file2.getFileType());

            if (compareType != 0) {
                return compareType;
            }

            return Long.compare(file2.getTimeAdded(), file1.getTimeAdded());
        };

        Collections.sort(filesList, sortByType);
    }
}
