package com.cyanogenmod.updatenotify.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

import com.cyanogenmod.updatenotify.MainActivity;

public class DeviceRegistrar {
    public static final String SENDER_ID = "cm.c2dm@gmail.com";
    public static final String STATUS_EXTRA = "Status";
    public static final int REGISTERED_STATUS = 1;
    public static final int UNREGISTERED_STATUS = 2;
    public static final int REGISTERED_FAILURE_STATUS = 3;
    public static final int UNREGISTERED_FAILURE_STATUS = 4;

    private static final String TAG = "CMUpdateNotify-DeviceRegistrar";

    public static void registerWithServer(final Context context, final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                String buildType = null;
                Intent updateUIIntent = new Intent(MainActivity.UPDATE_UI_ACTION);

                if (StringUtils.isRunningNightly()) {
                    buildType = "nightly";
                } else {
                    buildType = "stable";
                }

                try {
                    HttpResponse res = makeRegisterRequest(context, deviceRegistrationID);
                    if (res.getStatusLine().getStatusCode() == 200) {
                        saveDeviceRegistration(context, deviceRegistrationID);
                        saveBuildType(context, buildType);
                        updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
                        context.sendBroadcast(updateUIIntent);
                    } else {
                        Log.d(TAG, "Registration Error: " + String.valueOf(res.getStatusLine().getStatusCode()));
                        updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_FAILURE_STATUS);
                        context.sendBroadcast(updateUIIntent);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception", e);
                    updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_FAILURE_STATUS);
                    context.sendBroadcast(updateUIIntent);
                }
            }
        }).start();
    }

    public static void unregisterWithServer(final Context context, final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                Intent updateUIIntent = new Intent(MainActivity.UPDATE_UI_ACTION);

                try {
                    HttpResponse res = makeUnregisterRequest(context, deviceRegistrationID);
                    if (res.getStatusLine().getStatusCode() == 200) {
                        removeDeviceRegistration(context);
                        removeBuildType(context);
                        updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
                        context.sendBroadcast(updateUIIntent);
                    } else {
                        Log.d(TAG, "Unregistration Error: " + String.valueOf(res.getStatusLine().getStatusCode()));
                        updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_FAILURE_STATUS);
                        context.sendBroadcast(updateUIIntent);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception", e);
                    updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_FAILURE_STATUS);
                    context.sendBroadcast(updateUIIntent);
                }
            }
        }).start();
    }

    private static void saveDeviceRegistration(Context context, String deviceRegistrationID) {
        SharedPreferences.Editor editor = Preferences.get(context).edit();
        editor.putString(Preferences.DEVICEREGISTRATION_KEY, deviceRegistrationID);
        editor.commit();

        Log.d(TAG, "Saved deviceRegistrationID=" + deviceRegistrationID);
    }
    
    private static void removeDeviceRegistration(Context context) {
        SharedPreferences.Editor editor = Preferences.get(context).edit();
        editor.remove(Preferences.DEVICEREGISTRATION_KEY);
        editor.commit();

        Log.d(TAG, "Removed deviceRegistrationID");
    }
    
    private static void saveBuildType(Context context, String buildType) {
        SharedPreferences.Editor editor = Preferences.get(context).edit();
        editor.putString(Preferences.BUILDTYPE_KEY, buildType);
        editor.commit();

        Log.d(TAG, "Saved buildType=" + buildType);
    }
    
    private static void removeBuildType(Context context) {
        SharedPreferences.Editor editor = Preferences.get(context).edit();
        editor.remove(Preferences.BUILDTYPE_KEY);
        editor.commit();

        Log.d(TAG, "Removed buildType");
    }
    

    private static HttpResponse makeRegisterRequest(Context context, String deviceRegistrationID) throws Exception {
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair("deviceRegistrationId", deviceRegistrationID));

        if (StringUtils.isRunningNightly()) {
            params.add(new BasicNameValuePair("stable", "N"));
            params.add(new BasicNameValuePair("nightly", "Y"));
        } else {
            params.add(new BasicNameValuePair("stable", "Y"));
            params.add(new BasicNameValuePair("nightly", "N"));
        }
        params.add(new BasicNameValuePair("device", StringUtils.getDevice()));

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        params.add(new BasicNameValuePair("deviceId", deviceId));

        RegistrationClient client = new RegistrationClient();
        return client.registerRequest(params);
    }

    private static HttpResponse makeUnregisterRequest(Context context, String deviceRegistrationID) throws Exception {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("deviceRegistrationId", deviceRegistrationID));

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        params.add(new BasicNameValuePair("deviceId", deviceId));

        RegistrationClient client = new RegistrationClient();
        return client.unregisterRequest(params);
    }
}
