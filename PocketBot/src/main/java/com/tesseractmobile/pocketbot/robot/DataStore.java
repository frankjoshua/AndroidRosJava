package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/2015.
 */
public class DataStore{
    public static final String ROBOTS = "robots";
    public static final String USERS = "users";
    public static final String FIREBASE_URL = "https://boiling-torch-4457.firebaseio.com/";
    public static final String AUTH_DATA = "auth_data";
    static private DataStore instance;

    private PocketBotUser mUser;

    /** Stores user and robot data */
    private Firebase mFirebase;

    private AuthData mAuthData;

    private ArrayList<OnAuthCompleteListener> mOnAuthCompleteListeners = new ArrayList<OnAuthCompleteListener>();

    private FirebasePreferenceSync mFirebasePreferenceSync;

    private DataStore(final Context context){
        mFirebasePreferenceSync = new FirebasePreferenceSync(context);
    }

    static public void init(final Context context){
        if(instance == null){
            instance = new DataStore(context);
        }
    }

    static public DataStore get(){
        return instance;
    }

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
            public void onAuthenticated(AuthData authData) {
                mAuthData = authData;
                //Setup User
                mFirebase.child(USERS).child(authData.getUid()).child(AUTH_DATA).setValue(authData);
                //Start syncing preferences
                mFirebasePreferenceSync.start(getRobots());
                //Save current robot to list of allowed robots
                mFirebase.child(USERS).child(authData.getUid()).child(ROBOTS).child(robotId).setValue(true);
                //Let everyone know we are logged in
                for (OnAuthCompleteListener onAuthCompleteListener : mOnAuthCompleteListeners) {
                    onAuthCompleteListener.onAuthComplete();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                throw new UnsupportedOperationException(firebaseError.toString());
            }
        });
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
        if(mAuthData != null){
            return mFirebase.child(USERS).child(mAuthData.getUid());
        }
        throw new UnsupportedOperationException();
    }

    public void registerOnAuthCompleteListener(OnAuthCompleteListener onAuthCompleteListener) {
        mOnAuthCompleteListeners.add(onAuthCompleteListener);
        if(mAuthData != null){
            onAuthCompleteListener.onAuthComplete();
        }
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
        private RobotSettings mRobotSettings;
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
            firebase.child(mRobotId).child("settings").addChildEventListener(this);
            //Set inital preferences
            mRobotSettings = new RobotSettings(mContext);
            //Register for preference changes
            PocketBotSettings.registerOnSharedPreferenceChangeListener(mContext, this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Update local RobotSettings object
            if(key.equals(PocketBotSettings.ROBOT_ID)){
                //Remove old event listener
                mFirebase.child(mRobotId).child("settings").removeEventListener((ChildEventListener) this);
                //Get new Id
                mRobotId = sharedPreferences.getString(key, mRobotId);
                //Create new event listener
                mFirebase.child(mRobotId).child("settings").addChildEventListener(this);
                //Update Id
                mRobotSettings.robotId = mRobotId;
                return;
            } else if(key.equals(PocketBotSettings.SELECTED_FACE)){
                mRobotSettings.selectedFace = sharedPreferences.getInt(key, 0);
            } else if (key.equals(PocketBotSettings.FAST_TRACKING)) {
                mRobotSettings.fastFaceTracking = sharedPreferences.getBoolean(key, false);
            } else if (key.equals(PocketBotSettings.SHOW_PREVIEW)) {
                mRobotSettings.showVideoPreview = sharedPreferences.getBoolean(key, false);
            } else if (key.equals(PocketBotSettings.QB_ID)) {
                mRobotSettings.qbId = sharedPreferences.getInt(key, -1);
            } else if (key.equals(PocketBotSettings.API_AI_KEY)) {
                mRobotSettings.apiaiKey = sharedPreferences.getString(key, "");
            } else if (key.equals(PocketBotSettings.API_AI_TOKEN)) {
                mRobotSettings.apiaiToken = sharedPreferences.getString(key, "");
            } else if (key.equals(PocketBotSettings.ROBOT_NAME)) {
                mRobotSettings.robotName = sharedPreferences.getString(key, "");
            } else if (key.equals(PocketBotSettings.PASSWORD)) {
                mRobotSettings.password = sharedPreferences.getString(key, "");
            } else {
                //Setting not tracked
                return;
            }

            //Update setting to Firebase
            String id = sharedPreferences.getString(PocketBotSettings.ROBOT_ID, "Error");
            if(!mRobotSettings.syncInProgress && id.equals("Error") == false){
                mFirebase.child(id).child("settings").child("prefs").setValue(mRobotSettings);
            }
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            RobotSettings robotSettings = dataSnapshot.getValue(RobotSettings.class);
            mRobotSettings.sync(mContext, robotSettings);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            RobotSettings robotSettings = dataSnapshot.getValue(RobotSettings.class);
            mRobotSettings.sync(mContext, robotSettings);
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

        static private class RobotSettings {
            //Fields must be public for Jackson
            public int selectedFace;
            public boolean fastFaceTracking;
            public boolean showVideoPreview;
            public String robotName;
            public String apiaiKey;
            public String apiaiToken;
            public String password;
            public int qbId;
            public String robotId;
            public boolean syncInProgress;

            public RobotSettings(){
                //For Jackson
            }

            public RobotSettings(final Context context) {
                selectedFace = PocketBotSettings.getSelectedFace(context);
                fastFaceTracking = PocketBotSettings.getFastTrackingMode(context);
                showVideoPreview = PocketBotSettings.isShowPreview(context);
                robotName = PocketBotSettings.getRobotName(context);
                apiaiKey = PocketBotSettings.getApiAiKey(context);
                apiaiToken = PocketBotSettings.getApiAiToken(context);
                password = PocketBotSettings.getPassword(context);
                qbId = PocketBotSettings.getQuickBloxId(context);
                robotId = PocketBotSettings.getRobotId(context);
            }

            /**
             * Look for changes then update SharedPreferences if need
             * @param context
             * @param robotSettings
             */
            public void sync(final Context context, final RobotSettings robotSettings){
                syncInProgress = true;
                if(robotSettings.selectedFace != selectedFace){
                    selectedFace = robotSettings.selectedFace;
                    PocketBotSettings.setSelectedFace(context, selectedFace);
                }
                if(robotSettings.fastFaceTracking != fastFaceTracking){
                    fastFaceTracking = robotSettings.fastFaceTracking;
                    PocketBotSettings.setUseFastFaceTracking(context, fastFaceTracking);
                }
                if(robotSettings.showVideoPreview != showVideoPreview){
                    showVideoPreview = robotSettings.showVideoPreview;
                    PocketBotSettings.setShowPreview(context, showVideoPreview);
                }
                if(robotSettings.robotName != null && !robotSettings.robotName.equals(robotName)){
                    robotName = robotSettings.robotName;
                    PocketBotSettings.setRobotName(context, robotName);
                }
                if(robotSettings.apiaiKey != null && !robotSettings.apiaiKey.equals(apiaiKey)){
                    apiaiKey = robotSettings.apiaiKey;
                    PocketBotSettings.setApiAiKey(context, apiaiKey);
                }
                if(robotSettings.apiaiToken != null && !robotSettings.apiaiToken.equals(apiaiToken)){
                    apiaiToken = robotSettings.apiaiToken;
                    PocketBotSettings.setApiAiToken(context, apiaiToken);
                }
                if(robotSettings.password != null && !robotSettings.password.equals(password)){
                    password = robotSettings.password;
                    PocketBotSettings.setPassword(context, password);
                }
                if(robotSettings.qbId != qbId){
                    qbId = robotSettings.qbId;
                    PocketBotSettings.setQuickBloxId(context, qbId);
                }
                if(robotSettings.robotId != null && !robotSettings.robotId.equals(robotId)){
                    robotId = robotSettings.robotId;
                    PocketBotSettings.setRobotId(context, robotId);
                }
                syncInProgress = false;
            }

        }
    }
}
