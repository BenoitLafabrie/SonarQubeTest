package com.simplecity.amp_library.ui.screens.tagger;

import android.content.Context;
import android.support.v4.provider.DocumentFile;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TaggerTaskTest {

    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
    }

    @Test
    public void testBuilderMethods() {
        TaggerTask.TagCompletionListener listener = mock(TaggerTask.TagCompletionListener.class);
        List<String> paths = Arrays.asList("file1.mp3", "file2.mp3");
        List<DocumentFile> docFiles = Collections.emptyList();

        TaggerTask task = new TaggerTask(mockContext)
                .showAlbum(true)
                .showTrack(true)
                .title("title")
                .album("album")
                .artist("artist")
                .albumArtist("albumArtist")
                .year("2020")
                .track("1")
                .trackTotal("10")
                .disc("1")
                .discTotal("1")
                .lyrics("lyrics")
                .comment("comment")
                .genre("genre")
                .setPaths(paths)
                .setDocumentfiles(docFiles)
                .listener(listener);

        assertNotNull(task);
    }

    @Test
    public void testOnPostExecuteSuccess() {
        TaggerTask.TagCompletionListener listener = mock(TaggerTask.TagCompletionListener.class);
        TaggerTask task = new TaggerTask(mockContext).listener(listener);
        task.onPostExecute(true);
        verify(listener).onSuccess();
    }

    @Test
    public void testOnPostExecuteFailure() {
        TaggerTask.TagCompletionListener listener = mock(TaggerTask.TagCompletionListener.class);
        TaggerTask task = new TaggerTask(mockContext).listener(listener);
        task.onPostExecute(false);
        verify(listener).onFailure();
    }

    @Test
    public void testOnProgressUpdate() {
        TaggerTask.TagCompletionListener listener = mock(TaggerTask.TagCompletionListener.class);
        TaggerTask task = new TaggerTask(mockContext).listener(listener);
        task.onProgressUpdate(2);
        verify(listener).onProgress(3); // 2 + 1
    }
}
