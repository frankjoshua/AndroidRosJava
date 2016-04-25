package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 10/24/2015.
 */
public class SettingsFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
