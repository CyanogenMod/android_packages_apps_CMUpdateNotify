package com.cyanogenmod.updatenotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.cyanogenmod.updatenotify.utils.DeviceRegistrar;
import com.cyanogenmod.updatenotify.utils.Preferences;
import com.cyanogenmod.updatenotify.utils.StringUtils;
import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {
    private static final String TAG = "CMUpdateNotify-C2DMReceiver";

    public C2DMReceiver() {
        super(DeviceRegistrar.SENDER_ID);
    }

    @Override
    public void onRegistered(Context context, String registration) {
        DeviceRegistrar.registerWithServer(context, registration);
    }

    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Preferences.get(context);
        String deviceRegistrationID = prefs.getString(Preferences.DEVICEREGISTRATION_KEY, null);
        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        String oldVersion = StringUtils.getModVersion();
        String newVersion = extras.getString("modversion");
        String url = extras.getString("url");

        Log.d(TAG, "Got C2DM Message");
        Log.d(TAG, "oldVersion = " + oldVersion);
        Log.d(TAG, "newVersion = " + newVersion);
        Log.d(TAG, "url = " + url);

        if (StringUtils.isNightly(newVersion)) {
            sendNotification(context, oldVersion, newVersion, url);
        } else {
            if (StringUtils.compareModVersions(newVersion, oldVersion)) {
                sendNotification(context, oldVersion, newVersion, url);
            }
        }
    }

    private void sendNotification(Context context, String oldVersion, String newVersion, String url) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification
        CharSequence tickerText = getString(R.string.txt_notif_ticker);
        long when = System.currentTimeMillis();
        Notification notification = new Notification(R.drawable.icon, tickerText, when);
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Define expanded view
        CharSequence contentTitle = getString(R.string.txt_notif_title);
        CharSequence contentText = getString(R.string.txt_notif_long);
        Intent notificationIntent = new Intent(context, DownloadActivity.class);
        notificationIntent.putExtra("oldVersion", oldVersion);
        notificationIntent.putExtra("newVersion", newVersion);
        notificationIntent.putExtra("url", url);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        // Fire notification
        nm.notify(0, notification);
    }
}