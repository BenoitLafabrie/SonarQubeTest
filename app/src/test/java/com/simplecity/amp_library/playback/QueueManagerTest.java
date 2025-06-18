package com.simplecity.amp_library.playback;

import com.simplecity.amp_library.data.Repository;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.playback.constants.InternalIntents;
import com.simplecity.amp_library.rx.UnsafeAction;
import com.simplecity.amp_library.ui.screens.queue.QueueItem;
import com.simplecity.amp_library.utils.SettingsManager;
import com.simplecity.amp_library.playback.PlaybackSettingsManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QueueManagerTest {

    private MusicService.Callbacks mockCallbacks;
    private PlaybackSettingsManager mockPlaybackSettingsManager;
    private SettingsManager mockSettingsManager;
    private QueueManager queueManager;

    @Before
    @Before
    public void setUp() {
        mockCallbacks = mock(MusicService.Callbacks.class);
        Repository.SongsRepository mockSongsRepository = mock(Repository.SongsRepository.class);
        mockPlaybackSettingsManager = mock(PlaybackSettingsManager.class);
        mockSettingsManager = mock(SettingsManager.class);
        queueManager = new QueueManager(mockCallbacks, mockSongsRepository, mockPlaybackSettingsManager, mockSettingsManager);
    }
    @Test
    public void testSetRepeatMode() {
        queueManager.setRepeatMode(QueueManager.RepeatMode.ONE);
        assertEquals(QueueManager.RepeatMode.ONE, queueManager.repeatMode);
        verify(mockPlaybackSettingsManager).setRepeatMode(QueueManager.RepeatMode.ONE);
    }

    @Test
    public void testSetShuffleMode() {
        queueManager.setShuffleMode(QueueManager.ShuffleMode.ON);
        assertEquals(QueueManager.ShuffleMode.ON, queueManager.shuffleMode);
        verify(mockCallbacks, atLeastOnce()).notifyChange(anyString());
        verify(mockPlaybackSettingsManager).setShuffleMode(QueueManager.ShuffleMode.ON);
    }

    @Test
    public void testLoad() {
        Song song = new Song();
        song.id = 1;
        List<Song> songs = Collections.singletonList(song);
        UnsafeAction openCurrentAndNext = mock(UnsafeAction.class);

        queueManager.load(songs, 0, openCurrentAndNext);

        assertEquals(1, queueManager.playlist.size());
        assertEquals(0, queueManager.queuePosition);
        verify(openCurrentAndNext).run();
        verify(mockCallbacks, atLeastOnce()).notifyChange(anyString());
    }

    @Test
    public void testPrevious() {
        Song song = new Song();
        song.id = 1;
        queueManager.playlist.add(new QueueItem(song));
        queueManager.queuePosition = 0;
        queueManager.previous();
        assertEquals(queueManager.playlist.size() - 1, queueManager.queuePosition);
    }

    @Test
    public void testMoveQueueItem() {
        Song song1 = new Song(); song1.id = 1;
        Song song2 = new Song(); song2.id = 2;
        queueManager.playlist.add(new QueueItem(song1));
        queueManager.playlist.add(new QueueItem(song2));
        queueManager.queuePosition = 0;
        queueManager.moveQueueItem(0, 1);
        assertEquals(1, queueManager.queuePosition);
    }

    @Test
    public void testClearQueue() {
        queueManager.playlist.add(new QueueItem(new Song()));
        queueManager.queuePosition = 0;
        when(mockSettingsManager.getRememberShuffle()).thenReturn(false);
        queueManager.clearQueue();
        assertTrue(queueManager.playlist.isEmpty());
        assertEquals(-1, queueManager.queuePosition);
    }

    @Test
    public void testGetNextPosition() {
        Song song = new Song(); song.id = 1;
        queueManager.playlist.add(new QueueItem(song));
        queueManager.queuePosition = 0;
        queueManager.repeatMode = QueueManager.RepeatMode.OFF;
        assertEquals(-1, queueManager.getNextPosition(false));
        assertEquals(0, queueManager.getNextPosition(true));
    }

    @Test
    public void testEnqueueNext() {
        Song song = new Song(); song.id = 1;
        queueManager.playlist.add(new QueueItem(song));
        queueManager.queuePosition = 0;
        UnsafeAction setNextTrack = mock(UnsafeAction.class);
        UnsafeAction openCurrentAndNext = mock(UnsafeAction.class);

        Song newSong = new Song(); newSong.id = 2;
        queueManager.enqueue(Collections.singletonList(newSong), QueueManager.EnqueueAction.NEXT, setNextTrack, openCurrentAndNext);

        assertEquals(2, queueManager.playlist.size());
        verify(setNextTrack).run();
    }

    @Test
    public void testEnqueueLast() {
        Song song = new Song(); song.id = 1;
        queueManager.playlist.add(new QueueItem(song));
        queueManager.queuePosition = 0;
        UnsafeAction setNextTrack = mock(UnsafeAction.class);
        UnsafeAction openCurrentAndNext = mock(UnsafeAction.class);

        Song newSong = new Song(); newSong.id = 2;
        queueManager.enqueue(Collections.singletonList(newSong), QueueManager.EnqueueAction.LAST, setNextTrack, openCurrentAndNext);

        assertEquals(2, queueManager.playlist.size());
    }

    @Test
    public void testRemoveQueueItem() {
        Song song = new Song(); song.id = 1;
        QueueItem item = new QueueItem(song);
        queueManager.playlist.add(item);
        queueManager.queuePosition = 0;
        UnsafeAction stop = mock(UnsafeAction.class);
        UnsafeAction moveToNextTrack = mock(UnsafeAction.class);

        queueManager.removeQueueItem(item, stop, moveToNextTrack);
        assertTrue(queueManager.playlist.isEmpty());
        verify(stop).run();
    }

    @Test
    public void testRemoveQueueItems() {
        Song song1 = new Song(); song1.id = 1;
        Song song2 = new Song(); song2.id = 2;
        QueueItem item1 = new QueueItem(song1);
        QueueItem item2 = new QueueItem(song2);
        queueManager.playlist.add(item1);
        queueManager.playlist.add(item2);
        queueManager.queuePosition = 0;
        UnsafeAction stop = mock(UnsafeAction.class);
        UnsafeAction moveToNextTrack = mock(UnsafeAction.class);

        queueManager.removeQueueItems(Arrays.asList(item1, item2), stop, moveToNextTrack);
        assertTrue(queueManager.playlist.isEmpty());
        verify(stop).run();
    }

    @Test
    public void testSerializeAndDeserializePlaylist() {
        Song song = new Song(); song.id = 1;
        QueueItem item = new QueueItem(song);
        List<QueueItem> items = Collections.singletonList(item);
        String serialized = queueManager.serializePlaylist(items);
        List<QueueItem> deserialized = queueManager.deserializePlaylist(serialized, items);
        assertEquals(1, deserialized.size());
        assertEquals(song.id, deserialized.get(0).getSong().id);
    }
}
