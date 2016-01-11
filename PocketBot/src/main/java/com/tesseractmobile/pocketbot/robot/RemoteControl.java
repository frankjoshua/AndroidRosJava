package com.tesseractmobile.pocketbot.robot;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by josh on 12/1/2015.
 */
public class RemoteControl implements ChildEventListener, DataStore.OnAuthCompleteListener {
    public static final String CONTROL = "control";
    public static final String DATA = "data";
    //private Pubnub pubnub = new Pubnub("pub-c-2bd62a71-0bf0-4d53-bf23-298fd6b34c3e", "sub-c-75cdf46e-83e9-11e5-8495-02ee2ddab7fe");
    private Firebase mFirebaseListen;
    private Firebase mFirebaseTransmit;

    /** the pubnub channel to listen to */
    private String id;

    final private ArrayList<RemoteListener> mRemoteListeners = new ArrayList<RemoteListener>();
    /** Singleton */
    static private RemoteControl instance;

    private RemoteControl(final String id){
        setId(id);
    }

    /**
     * Initialize the RemoteControl
     * @param id
     */
    static public void init(final String id){
        if(instance == null){
            instance = new RemoteControl(id);
        }
    }

    /**
     * Singleton class
     * @return
     */
    static public RemoteControl get(){
        return instance;
    }

    /**
     * Register to listen for remote control messages
     * @param remoteListener
     */
    public void registerRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.add(remoteListener);
    }

    /**
     * Stop listening to remote messages
     * @param remoteListener
     */
    public void unregisterRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.remove(remoteListener);
    }

    /**
     * The channel id to listen to
     * @param id
     */
    public void setId(String id) {
        if(this.id != null){
            //Stop listening for firebase messages
            mFirebaseListen.removeEventListener(this);
            //Stop listening for auth registration
            DataStore.get().unregisterOnAuthCompleteListener(this);
        }
        //Set the ID
        this.id = id;
        //Listen for auth registration
        DataStore.get().registerOnAuthCompleteListener(this);
    }

    /**
     * Call when remote message is received
     * @param message
     */
    private void onObjectReceived(Object message) {
        for (RemoteListener remoteListener : mRemoteListeners) {
            remoteListener.onMessageReceived(message);
        }
    }

    /**
     * Pass data to remote robot
     * @param channel
     * @param object
     */
    public void send(String channel, Object object, final boolean asString) {
        //Send to firebase
        if(asString){
            mFirebaseTransmit.child(channel).child(CONTROL).child(DATA).setValue(object.toString());
        } else {
            mFirebaseTransmit.child(channel).child(CONTROL).child(DATA).setValue(object);
        }
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        onObjectReceived(dataSnapshot.getValue(SensorData.Control.class));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(final FirebaseError firebaseError) {
        //Firebase Cancelled
    }


    @Override
    public void onAuthComplete() {
        //Listen for messages from firebase
        mFirebaseListen = new Firebase(DataStore.FIREBASE_URL).child(CONTROL).child(id);
        mFirebaseListen.child(CONTROL).addChildEventListener(this);
        mFirebaseTransmit = new Firebase(DataStore.FIREBASE_URL).child(CONTROL);
    }
}
