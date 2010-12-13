package com.cyanogenmod.updatenotify.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
	public static final String DEVICEREGISTRATION_KEY = "deviceRegistrationID";
	public static final String BUILDTYPE_KEY = "buildType";
	
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("CM_UPDATENOTIFY_PREFS", 0);
    }
}