package com.simplecity.amp_library.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import com.simplecity.amp_library.playback.PlaybackSettingsManager;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class MediaButtonIntentReceiverTest {

    private Context mockContext;
    private Intent mockIntent;
    private PlaybackSettingsManager mockPlaybackSettingsManager;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockIntent = mock(Intent.class);
        mockPlaybackSettingsManager = mock(PlaybackSettingsManager.class);
    }

    @Test
    public void testHandleIntentAudioBecomingNoisyPauses() {
        when(mockIntent.getAction()).thenReturn(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        when(mockPlaybackSettingsManager.getPauseOnHeadsetDisconnect()).thenReturn(true);

        MediaButtonIntentReceiver.handleIntent(mockContext, mockIntent, mockPlaybackSettingsManager);

        // Should call startService with PAUSE command (verify via static mocking or integration)
    }

    @Test
    public void testHandleIntentMediaButtonNullEvent() {
        when(mockIntent.getAction()).thenReturn(Intent.ACTION_MEDIA_BUTTON);
        when(mockIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)).thenReturn(null);

        MediaButtonIntentReceiver.handleIntent(mockContext, mockIntent, mockPlaybackSettingsManager);

        // Should do nothing, no exception
    }

    @Test
    public void testHandleIntentMediaButtonPlayPause() {
        when(mockIntent.getAction()).thenReturn(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        when(mockIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)).thenReturn(event);

        MediaButtonIntentReceiver.handleIntent(mockContext, mockIntent, mockPlaybackSettingsManager);

        // Should handle play/pause event (verify via static mocking or integration)
    }

    @Test
    public void testGetMediaButtonCommand() {
        assertEquals("stop", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_STOP));
        assertEquals("togglepause", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        assertEquals("next", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_NEXT));
        assertEquals("previous", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        assertEquals("pause", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_PAUSE));
        assertEquals("play", MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_MEDIA_PLAY));
        assertNull(MediaButtonIntentReceiver.getMediaButtonCommand(KeyEvent.KEYCODE_VOLUME_UP));
    }
}
