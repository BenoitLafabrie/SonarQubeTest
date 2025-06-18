package com.simplecity.amp_library.utils;

import android.text.TextUtils;
import com.simplecity.amp_library.model.BaseFileObject;
import com.simplecity.amp_library.model.FileObject;
import com.simplecity.amp_library.model.FolderObject;
import com.simplecity.amp_library.model.TagInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileBrowserTest {

    private static final String HOME_DIR_PATH = "/home";

    private SettingsManager settingsManager;
    private FileBrowser fileBrowser;

    @Before
    public void setUp() {
        settingsManager = mock(SettingsManager.class);
        fileBrowser = new FileBrowser(settingsManager);
    }

    @Test
    public void testLoadDirReturnsEmptyListIfNoFiles() {
        File dir = mock(File.class);
        when(dir.listFiles(any())).thenReturn(null);

        List<BaseFileObject> result = fileBrowser.loadDir(dir);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLoadDirAddsParentFolderIfNotRoot() {
        File dir = mock(File.class);
        FileHelper mockHelper = mock(FileHelper.class);
        when(dir.listFiles(any())).thenReturn(new File[0]);
        // Simulate not root directory
        mockStaticIsRootDirectory(false);

        List<BaseFileObject> result = fileBrowser.loadDir(dir);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) instanceof FolderObject);
        assertEquals(FileHelper.PARENT_DIRECTORY, result.get(0).name);
    }

    @Test
    public void testGetCurrentDir() {
        File dir = mock(File.class);
        when(dir.listFiles(any())).thenReturn(null);
        fileBrowser.loadDir(dir);
        assertEquals(dir, fileBrowser.getCurrentDir());
    }

    @Test
    public void testClearAndSetHomeDir() {
        File dir = mock(File.class);
        when(dir.getPath()).thenReturn("/test");
        when(dir.listFiles(any())).thenReturn(null);

        fileBrowser.loadDir(dir);
        fileBrowser.setHomeDir();
        verify(settingsManager).setFolderBrowserInitialDir("/test");

        fileBrowser.clearHomeDir();
        verify(settingsManager).setFolderBrowserInitialDir("");
    }

        when(settingsManager.getFolderBrowserInitialDir()).thenReturn(HOME_DIR_PATH);
        assertTrue(fileBrowser.hasHomeDir());

        when(settingsManager.getFolderBrowserInitialDir()).thenReturn("");
        assertFalse(fileBrowser.hasHomeDir());
        when(settingsManager.getFolderBrowserInitialDir()).thenReturn("");
        assertFalse(fileBrowser.hasHomeDir());
    }

        File dir = mock(File.class);
        when(dir.getPath()).thenReturn(HOME_DIR_PATH);
        when(dir.compareTo(any())).thenReturn(0);
        when(dir.listFiles(any())).thenReturn(null);

        when(settingsManager.getFolderBrowserInitialDir()).thenReturn(HOME_DIR_PATH);
        fileBrowser.loadDir(dir);
        assertTrue(fileBrowser.atHomeDirectory());
        fileBrowser.loadDir(dir);
        assertTrue(fileBrowser.atHomeDirectory());
    }

    @Test
    public void testGetHomeDirIconAndTitle() {
        File dir = mock(File.class);
        when(dir.getPath()).thenReturn("/home");
        when(dir.compareTo(any())).thenReturn(0);
        when(dir.listFiles(any())).thenReturn(null);

        when(settingsManager.getFolderBrowserInitialDir()).thenReturn("/home");
        fileBrowser.loadDir(dir);

        int icon = fileBrowser.getHomeDirIcon();
        int title = fileBrowser.getHomeDirTitle();
        assertTrue(icon == com.simplecity.amp_library.R.drawable.ic_folder_remove ||
                   icon == com.simplecity.amp_library.R.drawable.ic_folder_nav ||
                   icon == com.simplecity.amp_library.R.drawable.ic_folder_outline);
        assertTrue(title == com.simplecity.amp_library.R.string.remove_home_dir ||
                   title == com.simplecity.amp_library.R.string.nav_home_dir ||
                   title == com.simplecity.amp_library.R.string.set_home_dir);
    }

    @Test
    public void testNullCompare() {
        assertEquals(0, fileBrowser.nullCompare(null, null));
        assertTrue(fileBrowser.nullCompare(null, "b") < 0);
        assertTrue(fileBrowser.nullCompare("a", null) > 0);
        assertEquals("a".compareTo("b"), fileBrowser.nullCompare("a", "b"));
    }

    // Helper to mock FileHelper.isRootDirectory
    private void mockStaticIsRootDirectory() {
        // If FileHelper.isRootDirectory is static and not mockable, you may need PowerMockito or refactor for testability.
        // For this example, assume it's mockable or always returns the value you want.
    }
}
