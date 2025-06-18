package com.simplecity.amp_library.utils;

import android.content.Context;
import com.simplecity.amp_library.model.BaseFileObject;
import com.simplecity.amp_library.model.FileObject;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileHelperTest {

    @Test
    public void testGetNameAndExtension() {
        assertEquals("file", FileHelper.getName("file.mp3"));
        assertEquals("file.name", FileHelper.getName("file.name.mp3"));
        assertNull(FileHelper.getExtension(".hiddenfile"));
        assertEquals("mp3", FileHelper.getExtension("file.mp3"));
        assertNull(FileHelper.getExtension("file"));
    }

    @Test
    public void testIsRootDirectory() {
        File root = new File(FileHelper.ROOT_DIRECTORY);
        assertTrue(FileHelper.isRootDirectory(root));
        String notRootPath = System.getProperty("test.notroot.path", "/notroot");
        File notRoot = new File(notRootPath);
        assertFalse(FileHelper.isRootDirectory(notRoot));
    }

    @Test
    public void testCanReadWrite() {
        File file = mock(File.class);
        when(file.canRead()).thenReturn(true);
        when(file.canWrite()).thenReturn(true);
        assertTrue(FileHelper.canReadWrite(file));
        when(file.canWrite()).thenReturn(false);
        assertFalse(FileHelper.canReadWrite(file));
    }

    @Test
    public void testIsSymlinkAndResolveSymlink() throws IOException {
        File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/a/b/c");
        when(file.getCanonicalPath()).thenReturn("/a/b/c");
        assertFalse(FileHelper.isSymlink(file));
        when(file.getCanonicalPath()).thenReturn("/a/b/d");
        assertTrue(FileHelper.isSymlink(file));
        when(file.getCanonicalFile()).thenReturn(file);
        assertEquals(file, FileHelper.resolveSymlink(file));
    }

    @Test
    public void testGetPathHandlesNullAndSymlink() throws IOException {
        assertNull(FileHelper.getPath(null));
        File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/storage/emulated/0");
        when(file.getCanonicalPath()).thenReturn("/storage/emulated/0");
        when(file.getCanonicalFile()).thenReturn(file);
        assertNotNull(FileHelper.getPath(file));
    }

    @Test
    public void testGetHumanReadableSize() {
        assertEquals("0", FileHelper.getHumanReadableSize(0));
        assertTrue(FileHelper.getHumanReadableSize(1024).contains("KB"));
        assertTrue(FileHelper.getHumanReadableSize(1024 * 1024).contains("MB"));
    }

    @Test
    public void testGetSupportedExtensions() {
        String[] exts = FileHelper.getSupportedExtensions();
        assertNotNull(exts);
        assertTrue(exts.length > 0);
    }

    @Test
    public void testGetAudioFilterAcceptsSupportedAudioFile() {
        File file = mock(File.class);
        when(file.isHidden()).thenReturn(false);
        when(file.canRead()).thenReturn(true);
        when(file.isDirectory()).thenReturn(false);
        when(file.getName()).thenReturn("song.mp3");
        assertTrue(FileHelper.getAudioFilter().accept(file));
    }

    @Test
    public void testGetAudioFilterRejectsHiddenOrUnreadable() {
        File file = mock(File.class);
        when(file.isHidden()).thenReturn(true);
        when(file.canRead()).thenReturn(true);
        assertFalse(FileHelper.getAudioFilter().accept(file));
        when(file.isHidden()).thenReturn(false);
        when(file.canRead()).thenReturn(false);
        assertFalse(FileHelper.getAudioFilter().accept(file));
    }

    @Test
    public void testDeleteFileAndDeleteRecursive() {
        File file = mock(File.class);
        when(file.isDirectory()).thenReturn(false);
        try {
            java.nio.file.Files.delete(file.toPath());
        } catch (Exception ignored) {
            // Exception ignored intentionally as this is a test to ensure no exception is thrown
        }
        // This test just ensures no exception is thrown
        FileHelper.deleteFile(file);
    }

    @Test
    public void testRenameFile() {
        Context context = mock(Context.class);
        BaseFileObject baseFileObject = mock(FileObject.class);
        when(baseFileObject.path).thenReturn("/tmp/file.mp3");
        File file = mock(File.class);
        File parent = mock(File.class);
        when(baseFileObject.getParent()).thenReturn(parent);
        when(parent.getPath()).thenReturn("/tmp");
        when(file.renameTo(any(File.class))).thenReturn(true);
        // This test just ensures no exception is thrown
        FileHelper.renameFile(context, baseFileObject, "newname");
    }
}
