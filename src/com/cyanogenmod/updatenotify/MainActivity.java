package com.cyanogenmod.updatenotify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.cyanogenmod.updatenotify.utils.DeviceRegistrar;
import com.cyanogenmod.updatenotify.utils.NightlyLicense;
import com.cyanogenmod.updatenotify.utils.Preferences;
import com.cyanogenmod.updatenotify.utils.StringUtils;
import com.google.android.c2dm.C2DMessaging;

public class MainActivity extends Activity {
    public static final String UPDATE_UI_ACTION = "com.cyanogenmod.updatenotify.UPDATE_UI";

    private Button mRegisterButton;
    private Button mUnregisterButton;
    private ProgressDialog mProgressDialog;

    private static final String TAG = "CMUpdateNotify-MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (StringUtils.isRunningNightly()) {
            setContentView(R.layout.main_nightly);
        } else {
            setContentView(R.layout.main_stable);
        }
        setTitle(R.string.title_main);

        mRegisterButton = (Button) findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(mOnRegisterListener);

        mUnregisterButton = (Button) findViewById(R.id.btn_unregister);
        mUnregisterButton.setOnClickListener(mOnUnregisterListener);

        checkBuildType();
        setupButtons();

        registerReceiver(mUpdateUIReceiver, new IntentFilter(UPDATE_UI_ACTION));
    }

    private void checkBuildType() {
        SharedPreferences settings = Preferences.get(this);
        SharedPreferences.Editor editor = settings.edit();
        String buildType = settings.getString(Preferences.BUILDTYPE_KEY, null);

        if (buildType != null && buildType.equals("nightly") && !StringUtils.isRunningNightly()) {
            Log.d(TAG, "Build Changed -- Removing settings.");
            editor.remove(Preferences.BUILDTYPE_KEY);
            editor.remove(Preferences.DEVICEREGISTRATION_KEY);
            editor.commit();
        }

        if (buildType != null && buildType.equals("stable") && StringUtils.isRunningNightly()) {
            Log.d(TAG, "Build Changed -- Removing settings.");
            editor.remove(Preferences.BUILDTYPE_KEY);
            editor.remove(Preferences.DEVICEREGISTRATION_KEY);
            editor.commit();
        }
    }

    private void setupButtons() {
        SharedPreferences settings = Preferences.get(this);
        String deviceRegistrationId = settings.getString(Preferences.DEVICEREGISTRATION_KEY, null);
        if (deviceRegistrationId == null) {
            mRegisterButton.setEnabled(true);
            mUnregisterButton.setEnabled(false);
        } else {
            mRegisterButton.setEnabled(false);
            mUnregisterButton.setEnabled(true);
        }

        NightlyLicense nl = new NightlyLicense(this);

        if (StringUtils.isRunningNightly() && !nl.validLicense()) {
            mRegisterButton.setEnabled(false);
            mUnregisterButton.setEnabled(false);
        }
    }

    private final OnClickListener mOnRegisterListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressDialog = ProgressDialog.show(MainActivity.this,
                    "", getString(R.string.txt_registering), true);
            mProgressDialog.show();

            mRegisterButton.setEnabled(false);
            C2DMessaging.register(MainActivity.this, DeviceRegistrar.SENDER_ID);
        }
    };

    private final OnClickListener mOnUnregisterListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressDialog = ProgressDialog.show(MainActivity.this,
                    "", getString(R.string.txt_unregistering), true);
            mProgressDialog.show();

            mUnregisterButton.setEnabled(false);
            C2DMessaging.unregister(MainActivity.this);
        }

    };

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA, 0);

            if (status == DeviceRegistrar.REGISTERED_STATUS) {
                mUnregisterButton.setEnabled(true);
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.txt_registration_success, 10).show();
            }

            if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
                mRegisterButton.setEnabled(true);
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.txt_unregistration_success, 10).show();
            }

            if (status == DeviceRegistrar.REGISTERED_FAILURE_STATUS) {
                mRegisterButton.setEnabled(true);
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.txt_registration_failure, 10).show();
            }

            if (status == DeviceRegistrar.UNREGISTERED_FAILURE_STATUS) {
                mUnregisterButton.setEnabled(true);
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.txt_unregistration_failure, 10).show();
            }
        }
    };
}
