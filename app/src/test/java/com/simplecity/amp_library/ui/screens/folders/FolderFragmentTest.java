package com.simplecity.amp_library.ui.screens.folders;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.simplecity.amp_library.utils.FileBrowser;
import com.simplecity.amp_library.utils.SettingsManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FolderFragmentTest {

    private FolderFragment fragment;
    private SettingsManager mockSettingsManager;
    private FileBrowser mockFileBrowser;
    private Menu mockMenu;
    private MenuItem mockMenuItem;

    @Before
    public void setUp() {
        fragment = new FolderFragment();
        mockSettingsManager = mock(SettingsManager.class);
        mockFileBrowser = mock(FileBrowser.class);
        mockToolbar = mock(Toolbar.class);
        Toolbar mockToolbar = mock(Toolbar.class);
        mockMenu = mock(Menu.class);
        mockMenuItem = mock(MenuItem.class);

        fragment.settingsManager = mockSettingsManager;
        fragment.fileBrowser = mockFileBrowser;
        fragment.toolbar = mockToolbar;

        when(mockToolbar.getMenu()).thenReturn(mockMenu);
        when(mockMenuItem.getItemId()).thenReturn(com.simplecity.amp_library.R.id.sort_files_default);

    @Test
    public void testNewInstance() {
        FolderFragment frag = FolderFragment.newInstance("Test", true);
        assertNotNull(frag);
        Bundle args = frag.getArguments();
        assertEquals("Test", args.getString("title"));
        assertTrue(args.getBoolean("displayed_in_tabs"));
    }

    @Test
    public void testOnMenuItemClick_SortFilesDefault() {
        when(mockMenuItem.getItemId()).thenReturn(com.simplecity.amp_library.R.id.sort_files_default);
        boolean result = fragment.onMenuItemClick(mockMenuItem);
        assertTrue(result);
        verify(mockSettingsManager).setFolderBrowserFilesSortOrder(anyInt());
    }

    @Test
    public void testOnMenuItemClick_FolderHomeDir() {
        when(mockMenuItem.getItemId()).thenReturn(com.simplecity.amp_library.R.id.folder_home_dir);
        when(mockFileBrowser.atHomeDirectory()).thenReturn(true);
        boolean result = fragment.onMenuItemClick(mockMenuItem);
        assertTrue(result);
        verify(mockFileBrowser).clearHomeDir();
    }

    @Test
    public void testOnMenuItemClick_ShowFilenames() {
        when(mockMenuItem.getItemId()).thenReturn(com.simplecity.amp_library.R.id.show_filenames);
        when(mockMenuItem.isChecked()).thenReturn(false);
        fragment.adapter = mock(com.simplecityapps.recycler_adapter.adapter.ViewModelAdapter.class);
        boolean result = fragment.onMenuItemClick(mockMenuItem);
        assertTrue(result);
        verify(mockSettingsManager).setFolderBrowserShowFileNames(true);
    }

    @Test
    public void testOnSaveInstanceState() {
        Bundle bundle = new Bundle();
        fragment.currentDir = "/test/path";
        fragment.onSaveInstanceState(bundle);
        assertEquals("/test/path", bundle.getString("current_dir"));
    }

    @Test
    public void testScreenName() {
        assertEquals("FolderFragment", fragment.screenName());
    }
}
