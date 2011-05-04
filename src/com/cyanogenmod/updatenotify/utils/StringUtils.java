package com.cyanogenmod.updatenotify.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.SystemProperties;

public class StringUtils {
    public static String getDevice() {
        return SystemProperties.get("ro.product.device");
    }
    
    public static String getModVersion() {
        return SystemProperties.get("ro.modversion");
    }
    
    public static boolean isRunningNightly() {
        return isNightly(getModVersion());
    }
    
    public static boolean compareModVersions(String newVersion, String oldVersion) {
        boolean newer = false;
        Pattern pattern = Pattern.compile("CyanogenMod-(\\d.\\d.\\d)(.?\\d?)-");
        Matcher match;

        // Match  new version.
        match = pattern.matcher(newVersion);
        while (match.find()) {
            if (match.groupCount() == 2) {
                newVersion = match.group(1).replace(".", "");
                if (match.group(2).equals("")) {
                    newVersion = newVersion + "0";
                } else {
                    newVersion = newVersion + match.group(2).replace(".", "");
                }
            }
        }

        // Match old version
        match = pattern.matcher(oldVersion);
        while (match.find()) {
            if (match.groupCount() == 2) {
                oldVersion = match.group(1).replace(".", "");
                if (match.group(2).equals("")) {
                    oldVersion = oldVersion + "0";
                } else {
                    oldVersion = oldVersion + match.group(2).replace(".", "");
                }
            }
        }

        if (Integer.valueOf(newVersion) > Integer.valueOf(oldVersion)) {
            newer = true;
        } else {
            newer = false;
        }

        return newer;
    }
    
    public static boolean isNightly(String modver) {
        boolean nightly = false;
        Pattern pattern = Pattern.compile("CyanogenMod-\\d-(\\d+)-NIGHTLY");
        Matcher match;
        
        match = pattern.matcher(modver);
        
        while (match.find()) {
            if (match.groupCount() == 1) {
                nightly = true;
            }
        }
        
        return nightly;
    }
}
