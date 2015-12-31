package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/2015.
 */
public class DataStore implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String ROBOTS = "robots";
    static private DataStore instance;

    private PocketBotUser mUser;

    private Firebase mFirebaseUsers;

    private AuthData mAuthData;

    private ArrayList<OnAuthCompleteListener> mOnAuthCompleteListeners = new ArrayList<OnAuthCompleteListener>();

    private FirebasePreferenceSync mFirebasePreferenceSync;

    private DataStore(){
        mFirebaseUsers = new Firebase("https://boiling-torch-4457.firebaseio.com/").child("users");
    }

    static public void init(final Context context){
        if(instance == null){
            instance = new DataStore();
            PocketBotSettings.registerOnSharedPreferenceChangeListener(context, instance);
            instance.mFirebasePreferenceSync = new FirebasePreferenceSync(context, instance.getRobots());
        }
    }

    static public DataStore get(){
        return instance;
    }

    public void setAuthToken(final String token) {
        mFirebaseUsers.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthData = authData;
                //Setup User
                final Firebase child = mFirebaseUsers.child(authData.getUid());
                child.child("AuthData").setValue(authData);
                child.child("Name").setValue(authData.getProviderData().get("displayName"));
                child.child("Email").setValue(authData.getProviderData().get("email"));
                child.child("ImageUrl").setValue(authData.getProviderData().get("profileImageURL"));
//                mFirebaseUsers.child(authData.getUid()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(FirebaseError firebaseError) {
//
//                    }
//                });
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

    public void addRobot(final String uuid, final String name){
        //Setup Robot
        Firebase robots = mFirebaseUsers.getParent().child(ROBOTS).child(uuid);
        robots.child("Name").setValue(name);
        robots.child("Owner").setValue("NOT_IMPLEMENTED");
        //Add to Users list of robots
        if(mAuthData != null){
            mFirebaseUsers.child(mAuthData.getUid()).child(ROBOTS).child(uuid).child("Name").setValue(name);
            mFirebaseUsers.child(mAuthData.getUid()).child(ROBOTS).child(uuid).child("Id").setValue(uuid);
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        String robotUUID = sharedPreferences.getString(PocketBotSettings.ROBOT_ID, "");
        if(key.equals(PocketBotSettings.ROBOT_NAME)){
            addRobot(robotUUID, sharedPreferences.getString(key, ""));
        }
    }

    private Firebase getRobots() {
        return mFirebaseUsers.getParent().child("robots");
    }


    /**
     * Returns a reference to the users robots
     * @return
     */
    public Firebase getRobotListRef() {
        if(mAuthData != null){
            //return mFirebaseUsers.child(mAuthData.getUid()).child(ROBOTS);
            return getRobots();
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

        public FirebasePreferenceSync(final Context context, final Firebase firebase) {
            this.mFirebase = firebase;
            this.mContext = context;
            //Listen for data changes on selected robot
            mRobotId = PocketBotSettings.getRobotId(context);
            firebase.child(mRobotId).child("settings").addChildEventListener(this);
            //Set inital preferences
            mRobotSettings = new RobotSettings(context);
            //Register for preference changes
            PocketBotSettings.registerOnSharedPreferenceChangeListener(context, this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Update local RobotSettings object
            if(key.equals(PocketBotSettings.ROBOT_ID)){
                mFirebase.child(mRobotId).child("settings").removeEventListener((ChildEventListener) this);
                mRobotId = sharedPreferences.getString(key, mRobotId);
                mFirebase.child(mRobotId).child("settings").addChildEventListener(this);
                mRobotSettings.robotId = mRobotId;
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
            //mFirebase.child(sharedPreferences.getString(PocketBotSettings.ROBOT_ID, "Error")).child("settings").child("prefs").setValue(GSON.toJson(mRobotSettings));
            String id = sharedPreferences.getString(PocketBotSettings.ROBOT_ID, "Error");
            if(id.equals("Error") == false){
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
            }

        }
    }
}
