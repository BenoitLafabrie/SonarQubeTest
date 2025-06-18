package com.simplecity.amp_library.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.playback.MusicService;
import com.simplecity.amp_library.playback.PlaybackSettingsManager;
import com.simplecity.amp_library.playback.constants.MediaButtonCommand;
import com.simplecity.amp_library.playback.constants.ServiceCommand;
import com.simplecity.amp_library.ui.screens.main.MainActivity;
import dagger.android.DaggerBroadcastReceiver;
import javax.inject.Inject;

public class MediaButtonIntentReceiver extends DaggerBroadcastReceiver {

    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;
    private static final int LONG_PRESS_DELAY = 1000;
    private static final int DOUBLE_CLICK = 800;

    private static int clickCounter = 0;
    private static long lastClickTime = 0;
    private static boolean down = false;
    private static boolean launched = false;

    private static PowerManager.WakeLock wakeLock = null;

    private static MediaButtonMessageHander mediaButtonMessageHander = new MediaButtonMessageHander();

    @Inject
    PlaybackSettingsManager playbackSettingsManager;

    public MediaButtonIntentReceiver() {
        // Constructor is intentionally empty because all initialization is handled by dependency injection and static fields.
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        handleIntent(context, intent, playbackSettingsManager);

        if (isOrderedBroadcast()) {
            abortBroadcast();
        }
    }

    public static void handleIntent(Context context, Intent intent, PlaybackSettingsManager playbackSettingsManager) {
        String intentAction = intent.getAction();

        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction) && playbackSettingsManager.getPauseOnHeadsetDisconnect()) {
            startService(context, MediaButtonCommand.PAUSE);
            return;
        }

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            handleMediaButtonEvent(context, event);
        }
    }

    private static void handleMediaButtonEvent(Context context, KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        long eventTime = event.getEventTime();

        String command = getMediaButtonCommand(keyCode);

        if (command == null) {
            return;
        }

        if (action == KeyEvent.ACTION_DOWN) {
            handleActionDown(context, event, keyCode, command, eventTime);
        } else {
            mediaButtonMessageHander.removeMessages(MSG_LONGPRESS_TIMEOUT);
            down = false;
        }

        releaseWakeLockIfHandlerIdle();
    }

    private static String getMediaButtonCommand(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_STOP:
                return MediaButtonCommand.STOP;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                return MediaButtonCommand.TOGGLE_PAUSE;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                return MediaButtonCommand.NEXT;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                return MediaButtonCommand.PREVIOUS;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                return MediaButtonCommand.PAUSE;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                return MediaButtonCommand.PLAY;
            default:
                return null;
        }
    }

    private static void handleActionDown(Context context, KeyEvent event, int keyCode, String command, long eventTime) {
        if (down) {
            if ((MediaButtonCommand.TOGGLE_PAUSE.equals(command) || MediaButtonCommand.PLAY.equals(command))
                    && lastClickTime != 0 && eventTime - lastClickTime > LONG_PRESS_DELAY) {
                acquireWakeLockAndSendMessage(context, mediaButtonMessageHander.obtainMessage(MSG_LONGPRESS_TIMEOUT, context), 0);
            }
        } else if (event.getRepeatCount() == 0) {
            handleFirstActionDown(context, keyCode, command, eventTime);
        }
    }

    private static void handleFirstActionDown(Context context, int keyCode, String command, long eventTime) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            handleHeadsetHook(context, eventTime);
        } else {
            startService(context, command);
        }
        launched = false;
        down = true;
    }

    private static void handleHeadsetHook(Context context, long eventTime) {
        if (eventTime - lastClickTime >= DOUBLE_CLICK) {
            clickCounter = 0;
        }

        clickCounter++;

        mediaButtonMessageHander.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);

        Message msg = mediaButtonMessageHander.obtainMessage(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, clickCounter, 0, context);

        long delay = clickCounter < 3 ? DOUBLE_CLICK : 0;
        if (clickCounter >= 3) {
            clickCounter = 0;
        }
        lastClickTime = eventTime;
        acquireWakeLockAndSendMessage(context, msg, delay);
    }

    static void beep(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_headset_beep", true)) {
            AsyncPlayer beepPlayer = new AsyncPlayer("BeepPlayer");
            Uri beepSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    context.getResources().getResourcePackageName(R.raw.beep) + '/' +
                    context.getResources().getResourceTypeName(R.raw.beep) + '/' +
                    context.getResources().getResourceEntryName(R.raw.beep));

            if (ShuttleUtils.hasMarshmallow()) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        // Could use AudioAttributes.ASSISTANCE_SONIFICATION here, since this represents a button press type action..
                        // However, that seems to play our audio a little too quietly (and the beep track is already adjusted to be relatively quiet).
                        // So let's just treat it as music, which will use the user's music stream's volume anyway.
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                beepPlayer.play(context, beepSoundUri, false, audioAttributes);
            } else {
                beepPlayer.play(context, beepSoundUri, false, AudioManager.STREAM_MUSIC);
            }
        }
    }

    static void startService(Context context, String command) {

        // If we're attempting to pause, and the service isn't already running, return early. This prevents an issue where
        // we call startForegroundService, and then we don't proceed to call startForeground() on the service, since the service
        // basically gets shutdown again due to the fact that we're not playing anything.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                && MediaButtonCommand.PAUSE.equals(command)
                && (MusicServiceConnectionUtils.serviceBinder == null 
                    || MusicServiceConnectionUtils.serviceBinder.getService() == null)) {
            return;
        }

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(ServiceCommand.COMMAND);
        intent.putExtra(MediaButtonCommand.CMD_NAME, command);
        intent.putExtra(MediaButtonCommand.FROM_MEDIA_BUTTON, true);

        if (MediaButtonCommand.PREVIOUS.equals(command)) {
            intent.putExtra(MediaButtonCommand.FORCE_PREVIOUS, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    static void acquireWakeLockAndSendMessage(Context context, Message msg, long delay) {
        if (wakeLock == null) {
            Context appContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Shuttle:HeadsetButton");
            wakeLock.setReferenceCounted(false);
        }

        // Make sure we don't indefinitely hold the wake lock under any circumstances
        wakeLock.acquire(10000);

        mediaButtonMessageHander.sendMessageDelayed(msg, delay);
    }

    static void releaseWakeLockIfHandlerIdle() {
        if (mediaButtonMessageHander.hasMessages(MSG_LONGPRESS_TIMEOUT) || mediaButtonMessageHander.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
            return;
        }

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private static class MediaButtonMessageHander extends Handler {

        @Override
        public void handleMessage(Message msg) {
            handleMessageStatic(msg);
        }

        private static void handleMessageStatic(Message msg) {

            if (msg.what == MSG_LONGPRESS_TIMEOUT) {
                if (!launched) {
                    Context context = (Context) msg.obj;
                    Intent intent = new Intent();
                    intent.setClass(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    launched = true;
                }
            } else if (msg.what == MSG_HEADSET_DOUBLE_CLICK_TIMEOUT) {
                int clickCount = msg.arg1;
                String command;

                if (clickCount == 1) {
                    command = MediaButtonCommand.TOGGLE_PAUSE;
                } else if (clickCount == 2) {
                    command = MediaButtonCommand.NEXT;
                } else if (clickCount == 3) {
                    command = MediaButtonCommand.PREVIOUS;
                } else {
                    command = null;
                }

                if (command != null) {
                    Context context = (Context) msg.obj;
                    if (MediaButtonCommand.NEXT.equals((command))) {
                        beep(context);
                    }
                    startService(context, command);
                }
            }
            releaseWakeLockIfHandlerIdle();
        }
    }
}
