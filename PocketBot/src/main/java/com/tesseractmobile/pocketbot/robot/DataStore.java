package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by josh on 12/27/2015.
 */
public class DataStore implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ROBOTS = "robots";
    public static final String USERS = "users";
    public static final String API_VERSION = "betaV2";
    public static final String BASE_FIREBASE_URL = "https://boiling-torch-4457.firebaseio.com/";
    public static final String FIREBASE_URL = BASE_FIREBASE_URL + API_VERSION + "/";
    public static final String AUTH_DATA = "auth_data";
    public static final String SETTINGS = "settings";
    public static final String LAST_ONLINE = "lastOnline";
    public static final String IS_CONNECTED = "isConnected";
    public static final String PREFS = "prefs";
    static private DataStore instance;

    /** Stores user and robot data */
    private Firebase mFirebase;

    private AuthData mAuthData;

    private ArrayList<OnAuthCompleteListener> mOnAuthCompleteListeners = new ArrayList<OnAuthCompleteListener>();

    private FirebasePreferenceSync mFirebasePreferenceSync;
    private String mRobotId;

    private DataStore(final Context context){
        mFirebasePreferenceSync = new FirebasePreferenceSync(context);
        PocketBotSettings.registerOnSharedPreferenceChangeListener(context, this);
    }

    static public DataStore init(final Context context){
        if(instance == null){
            instance = new DataStore(context);
        }
        return instance;
    }

//    static public DataStore get(){
//        return instance;
//    }

    /**
     * Signs in to Firebase using Google Auth
     * Also adds robot to list of allowed robots
     * @param robotId
     * @param token
     */
    public void setAuthToken(final String robotId, final String token) {
        mFirebase = new Firebase(FIREBASE_URL);
        mFirebase.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                mAuthData = authData;
                setupUser(authData, robotId);
                setupRobot(robotId);
                //Start syncing preferences
                mFirebasePreferenceSync.start(getRobots());
                //Let everyone know we are logged in
                for (OnAuthCompleteListener onAuthCompleteListener : mOnAuthCompleteListeners) {
                    onAuthCompleteListener.onAuthComplete();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e("DataStore", "Token: " + token);
                throw new UnsupportedOperationException(firebaseError.toString());
            }
        });
    }

    private void setupRobot(final String robotId) {

        if(mRobotId != null){
            //Disconnect last robot
            final Firebase lastRobotRef = getRobots().child(mRobotId);
            //Mark robot last connect status
            lastRobotRef.child(SETTINGS).child(LAST_ONLINE).setValue(ServerValue.TIMESTAMP);
            //Set robot online status
            lastRobotRef.child(SETTINGS).child(IS_CONNECTED).setValue(false);
            lastRobotRef.child(SETTINGS).child(IS_CONNECTED).setValue(false);
        }
        final Firebase robotRef = getRobots().child(robotId);
        //Save ID
        mRobotId = robotId;
        //Mark robot last connect status
        robotRef.child(SETTINGS).child(LAST_ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);
        //Set robot online status
        robotRef.child(SETTINGS).child(IS_CONNECTED).setValue(true);
        robotRef.child(SETTINGS).child(IS_CONNECTED).onDisconnect().setValue(false);
    }

    private void setupUser(final AuthData authData, final String robotId) {
        final Firebase userRef = mFirebase.child(USERS).child(authData.getUid());
        //Setup User
        userRef.child(AUTH_DATA).setValue(authData);
        //Save current robot to list of allowed robots
        userRef.child(ROBOTS).child(robotId).setValue(true);
        //Mark user last connect status
        userRef.child(LAST_ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);

        //Set user online status
        userRef.child(SETTINGS).child(IS_CONNECTED).setValue(true);
        userRef.child(IS_CONNECTED).onDisconnect().setValue(false);
    }

    private Firebase getRobots() {
        return mFirebase.child(ROBOTS);
    }

    /**
     * Returns a reference to the users robots
     * @return
     */
    public Firebase getRobotListRef() {
        if(mAuthData != null){
            return getRobots();
        }
        throw new UnsupportedOperationException();
    }

    public Firebase getUserListRef() {
        if (mAuthData != null){
            return mFirebase.child(USERS).child(mAuthData.getUid());
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Listen for completetion of user sign in
     * @param onAuthCompleteListener
     */
    public void registerOnAuthCompleteListener(OnAuthCompleteListener onAuthCompleteListener) {
        mOnAuthCompleteListeners.add(onAuthCompleteListener);
        if(mAuthData != null){
            onAuthCompleteListener.onAuthComplete();
        }
    }

    /**
     * Stop listening to user sign ins
     * @param onAuthCompleteListener
     */
    public void unregisterOnAuthCompleteListener(OnAuthCompleteListener onAuthCompleteListener) {
        mOnAuthCompleteListeners.remove(onAuthCompleteListener);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if(key.equals(PocketBotSettings.KEY_ROBOT_ID)){
            setupRobot(sharedPreferences.getString(PocketBotSettings.KEY_ROBOT_ID, ""));
        }
    }

    public void deleteRobot(final String robot_id) {
        //Delete robot from user
        final Firebase userRef = mFirebase.child(USERS).child(mAuthData.getUid());
        userRef.child(ROBOTS).child(robot_id).removeValue();
        //Delete robot
        getRobots().child(robot_id).removeValue();
        //Delete robot control
        mFirebase.child(RemoteControl.CONTROL).child(robot_id).removeValue();
    }

    public interface OnAuthCompleteListener {
        void onAuthComplete();
    }

    /**
     * Syncs settings between Firebase and Sharedpreferences
     */
    private static class FirebasePreferenceSync implements SharedPreferences.OnSharedPreferenceChangeListener, ChildEventListener {
        private String mRobotId;
        private Firebase mFirebase;
        private Context mContext;

        public FirebasePreferenceSync(final Context context) {
            this.mContext = context;
        }

        /**
         * Start syncing data
         * @param firebase
         */
        public void start(final Firebase firebase){
            this.mFirebase = firebase;
            //Listen for data changes on selected robot
            mRobotId = PocketBotSettings.getRobotId(mContext);
            firebase.child(mRobotId).child(SETTINGS).child(PREFS).addChildEventListener(this);
            //Set inital preferences
            onSharedPreferenceChanged(PocketBotSettings.getSharedPrefs(mContext), PocketBotSettings.KEY_ROBOT_NAME);
            onSharedPreferenceChanged(PocketBotSettings.getSharedPrefs(mContext), PocketBotSettings.KEY_QB_ID);
            mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).child(PocketBotSettings.KEY_ROBOT_ID).setValue(mRobotId);
            //Register for preference changes
            PocketBotSettings.registerOnSharedPreferenceChangeListener(mContext, this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Update local RobotSettings object
            if(key.equals(PocketBotSettings.KEY_ROBOT_ID)){
                //Remove old event listener
                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).removeEventListener((ChildEventListener) this);
                //Get new Id
                mRobotId = sharedPreferences.getString(key, mRobotId);
                //Create new event listener
                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).addChildEventListener(this);
                return;
            }
//            final Object value;
//            if(key.equals(PocketBotSettings.KEY_ROBOT_ID)){
//                //Remove old event listener
//                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).removeEventListener((ChildEventListener) this);
//                //Get new Id
//                mRobotId = sharedPreferences.getString(key, mRobotId);
//                //Create new event listener
//                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).addChildEventListener(this);
//                //Update Id
//                mRobotSettings.robotId = mRobotId;
//                return;
//            } else if(key.equals(PocketBotSettings.KEY_SELECTED_FACE)){
//                mRobotSettings.selectedFace = sharedPreferences.getInt(key, 0);
//            } else if (key.equals(PocketBotSettings.KEY_FAST_TRACKING)) {
//                mRobotSettings.fastFaceTracking = sharedPreferences.getBoolean(key, false);
//            } else if (key.equals(PocketBotSettings.KEY_SHOW_PREVIEW)) {
//                mRobotSettings.showVideoPreview = sharedPreferences.getBoolean(key, false);
//            } else if (key.equals(PocketBotSettings.KEY_QB_ID)) {
//                mRobotSettings.qbId = sharedPreferences.getInt(key, -1);
//            } else if (key.equals(PocketBotSettings.KEY_API_AI_KEY)) {
//                mRobotSettings.apiaiKey = sharedPreferences.getString(key, "");
//            } else if (key.equals(PocketBotSettings.KEY_API_AI_TOKEN)) {
//                mRobotSettings.apiaiToken = sharedPreferences.getString(key, "");
//            } else if (key.equals(PocketBotSettings.KEY_ROBOT_NAME)) {
//                mRobotSettings.robotName = sharedPreferences.getString(key, "");
//            } else if (key.equals(PocketBotSettings.KEY_PASSWORD)) {
//                mRobotSettings.password = sharedPreferences.getString(key, "");
//            } else {
//                //Setting not tracked
//                return;
//            }

            //Update setting to Firebase if robot id exist
            final String id = sharedPreferences.getString(PocketBotSettings.KEY_ROBOT_ID, "Error");
            if(id.equals("Error") == false){
                mFirebase.child(id).child(SETTINGS).child(PREFS).child(key).setValue(PocketBotSettings.getObject(sharedPreferences, key));
            }
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            snapshotToPreference(dataSnapshot);
//            RobotSettings robotSettings = dataSnapshot.getValue(RobotSettings.class);
//            mRobotSettings.sync(mContext, robotSettings);
        }

        private boolean snapshotToPreference(DataSnapshot dataSnapshot) {
            final Object value = dataSnapshot.getValue();
            final Class<?> valueClass = value.getClass();
            if(valueClass == String.class){
                return PocketBotSettings.getSharedPrefs(mContext).edit().putString(dataSnapshot.getKey(), (String) value).commit();
            } else if(valueClass == Boolean.class){
                return PocketBotSettings.getSharedPrefs(mContext).edit().putBoolean(dataSnapshot.getKey(), (Boolean) value).commit();
            } else if(valueClass == Long.class){
                return PocketBotSettings.getSharedPrefs(mContext).edit().putInt(dataSnapshot.getKey(), ((Long) value).intValue()).commit();
            } else {
                throw new UnsupportedOperationException("Unhandled class: " + valueClass.getSimpleName());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            snapshotToPreference(dataSnapshot);
//            RobotSettings robotSettings = dataSnapshot.getValue(RobotSettings.class);
//            mRobotSettings.sync(mContext, robotSettings);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }

//        static private class RobotSettings {
//            //Fields must be public for Jackson
//            public int selectedFace;
//            public boolean fastFaceTracking;
//            public boolean showVideoPreview;
//            public String robotName;
//            public String apiaiKey;
//            public String apiaiToken;
//            public String password;
//            public int qbId;
//            public String robotId;
//            public boolean syncInProgress;
//
//            public RobotSettings(){
//                //For Jackson
//            }
//
//            public RobotSettings(final Context context) {
//                selectedFace = PocketBotSettings.getSelectedFace(context);
//                fastFaceTracking = PocketBotSettings.getFastTrackingMode(context);
//                showVideoPreview = PocketBotSettings.isShowPreview(context);
//                robotName = PocketBotSettings.getRobotName(context);
//                apiaiKey = PocketBotSettings.getApiAiKey(context);
//                apiaiToken = PocketBotSettings.getApiAiToken(context);
//                password = PocketBotSettings.getPassword(context);
//                qbId = PocketBotSettings.getQuickBloxId(context);
//                robotId = PocketBotSettings.getRobotId(context);
//            }
//
//            /**
//             * Look for changes then update SharedPreferences if need
//             * @param context
//             * @param robotSettings
//             */
//            public void sync(final Context context, final RobotSettings robotSettings){
//                syncInProgress = true;
//                if(robotSettings.selectedFace != selectedFace){
//                    selectedFace = robotSettings.selectedFace;
//                    PocketBotSettings.setSelectedFace(context, selectedFace);
//                }
//                if(robotSettings.fastFaceTracking != fastFaceTracking){
//                    fastFaceTracking = robotSettings.fastFaceTracking;
//                    PocketBotSettings.setUseFastFaceTracking(context, fastFaceTracking);
//                }
//                if(robotSettings.showVideoPreview != showVideoPreview){
//                    showVideoPreview = robotSettings.showVideoPreview;
//                    PocketBotSettings.setShowPreview(context, showVideoPreview);
//                }
//                if(robotSettings.robotName != null && !robotSettings.robotName.equals(robotName)){
//                    robotName = robotSettings.robotName;
//                    PocketBotSettings.setRobotName(context, robotName);
//                }
//                if(robotSettings.apiaiKey != null && !robotSettings.apiaiKey.equals(apiaiKey)){
//                    apiaiKey = robotSettings.apiaiKey;
//                    PocketBotSettings.setApiAiKey(context, apiaiKey);
//                }
//                if(robotSettings.apiaiToken != null && !robotSettings.apiaiToken.equals(apiaiToken)){
//                    apiaiToken = robotSettings.apiaiToken;
//                    PocketBotSettings.setApiAiToken(context, apiaiToken);
//                }
//                if(robotSettings.password != null && !robotSettings.password.equals(password)){
//                    password = robotSettings.password;
//                    PocketBotSettings.setPassword(context, password);
//                }
//                if(robotSettings.qbId != qbId){
//                    qbId = robotSettings.qbId;
//                    PocketBotSettings.setQuickBloxId(context, qbId);
//                }
//                if(robotSettings.robotId != null && !robotSettings.robotId.equals(robotId)){
//                    robotId = robotSettings.robotId;
//                    PocketBotSettings.setRobotId(context, robotId);
//                }
//                syncInProgress = false;
//            }
//
//        }
    }
}
