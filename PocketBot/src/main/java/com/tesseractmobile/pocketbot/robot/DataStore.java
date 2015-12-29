package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.activities.fragments.ControlFaceFragment;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by josh on 12/27/2015.
 */
public class DataStore implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String ROBOTS = "robots";
    static private DataStore instance;

    private PocketBotUser mUser;

    private Firebase mFirebaseRef;

    private AuthData mAuthData;

    private ArrayList<OnAuthCompleteListener> mOnAuthCompleteListeners = new ArrayList<OnAuthCompleteListener>();

    private DataStore(){
        mFirebaseRef = new Firebase("https://boiling-torch-4457.firebaseio.com/").child("users");
    }

    static public void init(final Context context){
        if(instance == null){
            instance = new DataStore();
            PocketBotSettings.registerOnSharedPreferenceChangeListener(context, instance);
        }
    }

    static public DataStore get(){
        return instance;
    }

    public void setAuthToken(final String token) {
        mFirebaseRef.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthData = authData;
                //Setup User
                final Firebase child = mFirebaseRef.child(authData.getUid());
                child.child("AuthData").setValue(authData);
                child.child("Name").setValue(authData.getProviderData().get("displayName"));
                child.child("Email").setValue(authData.getProviderData().get("email"));
                child.child("ImageUrl").setValue(authData.getProviderData().get("profileImageURL"));
//                mFirebaseRef.child(authData.getUid()).addValueEventListener(new ValueEventListener() {
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
                for(OnAuthCompleteListener onAuthCompleteListener : mOnAuthCompleteListeners){
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
        Firebase robots = mFirebaseRef.getParent().child(ROBOTS).child(uuid);
        robots.child("Name").setValue(name);
        robots.child("Owner").setValue("NOT_IMPLEMENTED");
        //Add to Users list of robots
        if(mAuthData != null){
            mFirebaseRef.child(mAuthData.getUid()).child(ROBOTS).child(uuid).child("Name").setValue(name);
            mFirebaseRef.child(mAuthData.getUid()).child(ROBOTS).child(uuid).child("Id").setValue(uuid);
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if(key.equals(PocketBotSettings.ROBOT_NAME)){
            addRobot(sharedPreferences.getString(PocketBotSettings.ROBOT_ID, ""), sharedPreferences.getString(key, ""));
        }
    }

    public Firebase getRobotListRef() {
        if(mAuthData != null){
            return mFirebaseRef.child(mAuthData.getUid()).child(ROBOTS);
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
}
