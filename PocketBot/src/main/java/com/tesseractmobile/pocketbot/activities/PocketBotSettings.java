package com.tesseractmobile.pocketbot.activities;

import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.tesseractmobile.pocketbot.robot.CommandContract;

/**
 * Created by josh on 9/10/2015.
 */
public class PocketBotSettings {

    public static final String SHOW_PREVIEW = CommandContract.PARAM_PREVIEW;
    public static final String SHOW_TEXT_PREVIEW = CommandContract.PARAM_TEXT_PREVIEW;
    public static final String USE_BLUETOOTH = CommandContract.PARAM_BLUETOOTH;
    public static final boolean DEFAULT_SHOW_TEXT_PREVIEW = true;
    public static final boolean DEFAULT_SHOW_PREVIEW = true;
    public static final boolean DEFAULT_USE_BLUETOOTH = false;

    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isShowPreview(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_PREVIEW, DEFAULT_SHOW_PREVIEW);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setShowPreview(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SHOW_PREVIEW, b).commit();
    }

    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isShowTextPreview(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_TEXT_PREVIEW, DEFAULT_SHOW_TEXT_PREVIEW);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setShowTextPreview(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SHOW_TEXT_PREVIEW, b).commit();
    }

    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isUseBluetooth(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(USE_BLUETOOTH, DEFAULT_USE_BLUETOOTH);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setUseBluetooth(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(USE_BLUETOOTH, b).commit();
    }

    /**
     * Listen for preference changes
     * @param context
     * @param onSharedPreferenceChangeListener
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context, final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /**
     * Stop listening for preference changes
     * @param context
     * @param onSharedPreferenceChangeListener
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context, final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}
