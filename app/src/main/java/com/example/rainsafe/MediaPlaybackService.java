package com.example.rainsafe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

public class MediaPlaybackService extends Service {
    private MediaSessionCompat mediaSession;
    public static final String CHANNEL_ID = "media_playback_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Inisialisasi MediaSession
        mediaSession = new MediaSessionCompat(this, "RainSafeMediaSession");

        // 2. Set Metadata (Judul, Artis, Gambar)
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "RainSafe Monitoring")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Smart Laundry System")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, 
                        BitmapFactory.decodeResource(getResources(), R.drawable.download_removebg_preview))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100000) // Durasi untuk Seek Bar
                .build());

        // 3. Set Callback untuk tombol kontrol
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                startForeground(1, createNotification(true));
            }

            @Override
            public void onPause() {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                startForeground(1, createNotification(false));
                // stopForeground(false); // Biarkan notifikasi tetap ada tapi bisa di-dismiss
            }

            @Override
            public void onSkipToNext() {
                // Logika skip next (misal ganti sensor yang dipantau)
            }
        });

        mediaSession.setActive(true);
        createNotificationChannel();
    }

    private void updatePlaybackState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | 
                           PlaybackStateCompat.ACTION_PAUSE | 
                           PlaybackStateCompat.ACTION_SKIP_TO_NEXT | 
                           PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                           PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(state, 0, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private Notification createNotification(boolean isPlaying) {
        // Catatan: Pastikan ic_play_arrow dan ic_pause sudah ada di drawable. 
        // Jika belum ada, gunakan ic_refresh atau ic_arrow_forward sebagai placeholder.
        int iconPlayPause = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        
        Intent intent = new Intent(this, DashboardActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("RainSafe Monitoring")
                .setContentText("System Active")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download_removebg_preview))
                .setContentIntent(contentIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Prev", null))
                .addAction(new NotificationCompat.Action(iconPlayPause, isPlaying ? "Pause" : "Play", null))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", null))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Media Control", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
        startForeground(1, createNotification(true));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        mediaSession.release();
        super.onDestroy();
    }
}
