package com.simplecity.amp_library.utils;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.interfaces.FileType;
import com.simplecity.amp_library.model.BaseFileObject;
import com.simplecity.amp_library.model.FileObject;
import com.simplecity.amp_library.model.FolderObject;
import com.simplecity.amp_library.model.TagInfo;
import com.simplecity.amp_library.utils.sorting.SortManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowser {


    @Nullable
    private File currentDir;

    private SettingsManager settingsManager;

    public FileBrowser(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    /**
     * Loads the specified folder.
     *
     * @param directory The file object to points to the directory to load.
     * @return An {@link List<BaseFileObject>} object that holds the data of the specified directory.
     */
    @WorkerThread
    public List<BaseFileObject> loadDir(File directory) {
        ThreadUtils.ensureNotOnMainThread();
        currentDir = directory;

        List<BaseFileObject> folderObjects = new ArrayList<>();
        List<BaseFileObject> fileObjects = new ArrayList<>();

        processFiles(directory, folderObjects, fileObjects);

        sortAndCombine(folderObjects, fileObjects);

        addParentFolderIfNeeded(folderObjects);

        return folderObjects;
    }

    private void processFiles(File directory, List<BaseFileObject> folderObjects, List<BaseFileObject> fileObjects) {
        File[] files = directory.listFiles(FileHelper.getAudioFilter());
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (shouldSkipFile(file)) {
                continue;
            }
            if (file.isDirectory()) {
                addFolderObject(folderObjects, file);
            } else {
                addFileObject(fileObjects, file);
            }
        }
    }

    private void addFolderObject(List<BaseFileObject> folderObjects, File file) {
        FolderObject folderObject = createFolderObject(file);
        if (!folderObjects.contains(folderObject)) {
            folderObjects.add(folderObject);
        }
    }

    private void addFileObject(List<BaseFileObject> fileObjects, File file) {
        FileObject fileObject = createFileObject(file);
        if (!fileObjects.contains(fileObject)) {
            fileObjects.add(fileObject);
        }
    }

    private void sortAndCombine(List<BaseFileObject> folderObjects, List<BaseFileObject> fileObjects) {
        sortFileObjects(fileObjects);
        sortFolderObjects(folderObjects);

        if (!settingsManager.getFolderBrowserFilesAscending()) {
            Collections.reverse(fileObjects);
        }

        if (!settingsManager.getFolderBrowserFoldersAscending()) {
            Collections.reverse(folderObjects);
        }

        folderObjects.addAll(fileObjects);
    }

    private boolean shouldSkipFile(File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles(FileHelper.getAudioFilter());
            return subFiles == null || subFiles.length == 0;
        } else {
            return TextUtils.isEmpty(FileHelper.getExtension(file.getName()));
        }
    }

    private FolderObject createFolderObject(File file) {
        FolderObject folderObject = new FolderObject();
        folderObject.path = FileHelper.getPath(file);
        folderObject.name = file.getName();
        File[] listOfFiles = file.listFiles(FileHelper.getAudioFilter());
        if (listOfFiles != null && listOfFiles.length > 0) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isDirectory()) {
                    folderObject.folderCount++;
                } else {
                    folderObject.fileCount++;
                }
            }
        }
        return folderObject;
    }

    private FileObject createFileObject(File file) {
        FileObject fileObject = new FileObject();
        fileObject.path = FileHelper.getPath(file);
        fileObject.name = FileHelper.getName(file.getName());
        fileObject.size = file.length();
        fileObject.extension = FileHelper.getExtension(file.getName());
        fileObject.tagInfo = new TagInfo(fileObject.path);
        return fileObject;
    }

    private void addParentFolderIfNeeded(List<BaseFileObject> folderObjects) {
        if (!FileHelper.isRootDirectory(currentDir)) {
            FolderObject parentObject = new FolderObject();
            parentObject.fileType = FileType.PARENT;
            parentObject.name = FileHelper.PARENT_DIRECTORY;
            parentObject.path = FileHelper.getPath(currentDir) + File.separator + FileHelper.PARENT_DIRECTORY;
            folderObjects.add(0, parentObject);
        }
    }

    @Nullable
    public File getCurrentDir() {
        return currentDir;
    }

    @WorkerThread
    public File getInitialDir() {
        ThreadUtils.ensureNotOnMainThread();

        File dir = getInitialDirFromSettings();
        if (dir != null) {
            return dir;
        }

        dir = getStorageDirectory();
        dir = getMusicDirectoryIfExists(dir);

        return dir;
    }

    private File getInitialDirFromSettings() {
        String settingsDir = settingsManager.getFolderBrowserInitialDir();
        if (settingsDir != null) {
            File file = new File(settingsDir);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private File getStorageDirectory() {
        File dir = new File("/");
        String[] files = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("storage"));

        if (files != null && files.length > 0) {
            dir = new File(dir + File.separator + files[0]);
            dir = getExtSdCardDirectoryIfExists(dir);
        } else {
            dir = getExternalStorageDirectoryIfMounted(dir);
        }
        return dir;
    }

    private File getExtSdCardDirectoryIfExists(File dir) {
        String[] files = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("extsdcard"));
        if (files != null && files.length > 0) {
            return new File(dir + File.separator + files[0]);
        } else {
            return getExternalStorageDirectoryIfMounted(dir);
        }
    }

    private File getExternalStorageDirectoryIfMounted(File dir) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory();
        }
        return dir;
    }

    private File getMusicDirectoryIfExists(File dir) {
        if (dir != null) {
            String[] files = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("music"));
            if (files != null && files.length > 0) {
                return new File(dir + File.separator + files[0]);
            }
        }
        return dir;
    }

    public void sortFolderObjects(List<BaseFileObject> baseFileObjects) {

        switch (settingsManager.getFolderBrowserFoldersSortOrder()) {
            case SortManager.SortFolders.COUNT:
                Collections.sort(baseFileObjects, fileCountComparator());
                Collections.sort(baseFileObjects, folderCountComparator());
                break;

            case SortManager.SortFolders.DEFAULT:
            default:
                Collections.sort(baseFileObjects, filenameComparator());
                break;
        }
    }

    public void sortFileObjects(List<BaseFileObject> baseFileObjects) {
        switch (settingsManager.getFolderBrowserFilesSortOrder()) {
            case SortManager.SortFiles.SIZE:
                Collections.sort(baseFileObjects, sizeComparator());
                break;
            case SortManager.SortFiles.FILE_NAME:
                Collections.sort(baseFileObjects, filenameComparator());
                break;
            case SortManager.SortFiles.ARTIST_NAME:
                Collections.sort(baseFileObjects, artistNameComparator());
                break;
            case SortManager.SortFiles.ALBUM_NAME:
                Collections.sort(baseFileObjects, albumNameComparator());
                break;
            case SortManager.SortFiles.TRACK_NAME:
                Collections.sort(baseFileObjects, trackNameComparator());
                break;
            case SortManager.SortFiles.DEFAULT:
            default:
                Collections.sort(baseFileObjects, trackNumberComparator());
                Collections.sort(baseFileObjects, albumNameComparator());
                Collections.sort(baseFileObjects, artistNameComparator());
                break;
        }
    }

    public void clearHomeDir() {
        settingsManager.setFolderBrowserInitialDir("");
    }

    public void setHomeDir() {
        if (currentDir != null) {
            settingsManager.setFolderBrowserInitialDir(currentDir.getPath());
        }
    }

    public File getHomeDir() {
        return new File(settingsManager.getFolderBrowserInitialDir());
    }

    public boolean hasHomeDir() {
        return !TextUtils.isEmpty(getHomeDir().getPath());
    }

    public boolean atHomeDirectory() {
        final File currDir = getCurrentDir();
        final File homeDir = getHomeDir();
        return currDir != null && homeDir != null && currDir.compareTo(homeDir) == 0;
    }

    public int getHomeDirIcon() {
        int icon = R.drawable.ic_folder_outline;
        if (atHomeDirectory()) {
            icon = R.drawable.ic_folder_remove;
        } else if (hasHomeDir()) {
            icon = R.drawable.ic_folder_nav;
        }
        return icon;
    }

    public int getHomeDirTitle() {
        int title = R.string.set_home_dir;
        if (atHomeDirectory()) {
            title = R.string.remove_home_dir;
        } else if (hasHomeDir()) {
            title = R.string.nav_home_dir;
        }
        return title;
    }

    private Comparator sizeComparator() {
        return (Comparator<BaseFileObject>) (lhs, rhs) -> (int) (rhs.size - lhs.size);
    }

    private Comparator<BaseFileObject> filenameComparator() {
        return (lhs, rhs) -> lhs.name.compareToIgnoreCase(rhs.name);
    }

    //    private Comparator durationComparator() {
    //        return (Comparator<FileObject>) (lhs, rhs) -> (int) (rhs.duration - lhs.duration);
    //    }

    private Comparator trackNumberComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> lhs.tagInfo.trackNumber - rhs.tagInfo.trackNumber;
    }

    private Comparator folderCountComparator() {
        return (Comparator<FolderObject>) (lhs, rhs) -> rhs.folderCount - lhs.folderCount;
    }

    private Comparator fileCountComparator() {
        return (Comparator<FolderObject>) (lhs, rhs) -> rhs.fileCount - lhs.fileCount;
    }

    private Comparator artistNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.artistName == null || rhs.tagInfo.artistName == null) {
                return nullCompare(lhs.tagInfo.artistName, rhs.tagInfo.artistName);
            }
            return lhs.tagInfo.artistName.compareToIgnoreCase(rhs.tagInfo.artistName);
        };
    }

    private Comparator albumNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.albumName == null || rhs.tagInfo.albumName == null) {
                return nullCompare(lhs.tagInfo.albumName, rhs.tagInfo.albumName);
            }
            return lhs.tagInfo.albumName.compareToIgnoreCase(rhs.tagInfo.albumName);
        };
    }

    private Comparator trackNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.trackName == null || rhs.tagInfo.trackName == null) {
                return nullCompare(lhs.tagInfo.trackName, rhs.tagInfo.trackName);
            }
            return lhs.tagInfo.trackName.compareToIgnoreCase(rhs.tagInfo.trackName);
        };
    }

    <T extends Comparable<T>> int nullCompare(T a, T b) {
        return a == null ? (b == null ? 0 : Integer.MIN_VALUE) : (b == null ? Integer.MAX_VALUE : a.compareTo(b));
    }
}
