package com.pdfconverter.jpg2pdf.pdf.converter.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;

public class FirebaseRemoteUtils {
    private static final String FREE_COUNTRY_LIST = "free_country_list";

    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FirebaseRemoteUtils() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        if (BuildConfig.DEBUG) {
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        }
    }

    public void fetchRemoteConfig(Activity context, Runnable afterRunnable) {

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(context, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            afterRunnable.run();
                        } else {
                            afterRunnable.run();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        afterRunnable.run();
                    }
                });
    }

    public ArrayList<String> getFreeCountryList(Context context) {
        String listData = mFirebaseRemoteConfig.getString(FREE_COUNTRY_LIST);
        listData = listData.toLowerCase();

        if (listData == null || listData.length() == 0) {
            return new ArrayList<>();
        }

        String[] data = listData.split(",");
        if (data.length == 0) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(data));
    }
}
