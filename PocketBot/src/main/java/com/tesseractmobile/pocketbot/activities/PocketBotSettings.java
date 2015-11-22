package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.FragmentActivity;

import com.tesseractmobile.pocketbot.activities.fragments.SignInFragment;
import com.tesseractmobile.pocketbot.robot.CommandContract;

/**
 * Created by josh on 9/10/2015.
 */
public class PocketBotSettings {

    public static final String SHOW_PREVIEW = CommandContract.PARAM_PREVIEW;
    public static final String SHOW_TEXT_PREVIEW = CommandContract.PARAM_TEXT_PREVIEW;
    public static final String USE_BLUETOOTH = CommandContract.PARAM_BLUETOOTH;
    public static final String SELECTED_FACE = "selected_face";
    public static final boolean DEFAULT_SHOW_TEXT_PREVIEW = true;
    public static final boolean DEFAULT_SHOW_PREVIEW = true;
    public static final boolean DEFAULT_USE_BLUETOOTH = false;
    public static final int DEFAULT_FACE_ID = 0;
    public static final String USER_NAME = "user_name";
    public static final String PASSWORD = "password";
    public static final String SIGNED_IN = "signed_in";
    private static final String USER_ID = "user_id";
    private static final String API_AI_KEY = "api_ai_key";
    public static final String API_AI_DEFAULT_KEY = "1eca9ad4-74e8-4d3a-afea-7131df82d19b";
    private static final String API_AI_TOKEN = "api_ai_token";
    public static final String API_AI_DEFAULT_TOKEN = "443dddf4747d4408b0e9451d4d53f201";

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
     *
     * @param context
     * @param faceId
     * @return
     */
    static public boolean setSelectedFace(final Context context, final int faceId){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SELECTED_FACE, faceId).commit();
    }

    /**
     *
     * @param context
     * @return
     */
    static public int getSelectedFace(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SELECTED_FACE, 0);
    }

    public static boolean setUserName(final Context context, final String userName) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(USER_NAME, userName).commit();
    }

    public static String getUserName(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_NAME, "");
    }

    public static boolean setPassword(final Context context, final String password) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PASSWORD, password).commit();
    }

    public static String getPassword(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PASSWORD, "");
    }

    public static boolean setSignedIn(final Context context, boolean b) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SIGNED_IN, b).commit();
    }

    /**
     *
     * @param context
     * @return true if user is signed in
     */
    public static boolean isSignedIn(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SIGNED_IN, false);
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

    /**
     * Return last user ID
     * @param context
     * @return
     */
    public static String getLastUserId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_ID, "");
    }

    public static boolean setLastUserId(Context context, String userId) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(USER_ID, userId).commit();
    }

    public static String getApiAiKey(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(API_AI_KEY, API_AI_DEFAULT_KEY);
    }

    public static boolean setApiAiKey(final Context context, final String key){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(API_AI_KEY, key).commit();
    }

    public static String getApiAiToken(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(API_AI_TOKEN, API_AI_DEFAULT_TOKEN);
    }

    public static boolean setApiAiToken(final Context context, final String token){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(API_AI_TOKEN, token).commit();
    }
}
