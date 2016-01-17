package com.tesseractmobile.pocketbot.robot;

import android.provider.ContactsContract;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import org.json.JSONObject;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by josh on 12/1/2015.
 */
public class RemoteControl implements ChildEventListener, DataStore.OnAuthCompleteListener {
    public static final String CONTROL = "control";
    public static final String DATA = "data";
    private static final String CONNECTED = "connected";
    private static final String TIMESTAMP = "time_stamp";
    //private Pubnub pubnub = new Pubnub("pub-c-2bd62a71-0bf0-4d53-bf23-298fd6b34c3e", "sub-c-75cdf46e-83e9-11e5-8495-02ee2ddab7fe");
    private Firebase mFirebaseListen;
    private Firebase mFirebaseTransmit;

    /** the pubnub channel to listen to */
    private String id;

    final private ArrayList<RemoteListener> mRemoteListeners = new ArrayList<RemoteListener>();
    /** Singleton */
    static private RemoteControl instance;
    private Long mTimeStamp;
    private long mTimeSinceLastControl;
    private String mTransmitUUID;

    private RemoteControl(final String id){
        setId(id);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeStamp();
                }
            }
        });
        thread.start();
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
    public synchronized void registerRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.add(remoteListener);
    }

    /**
     * Stop listening to remote messages
     * @param remoteListener
     */
    public synchronized void unregisterRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.remove(remoteListener);
    }

    /**
     * The channel id to listen to
     * @param id
     */
    private void setId(String id) {
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
    private synchronized void onObjectReceived(Object message) {
        for (RemoteListener remoteListener : mRemoteListeners) {
            remoteListener.onMessageReceived(message);
        }
    }

    /**
     * Call when connection is lost
     */
    private synchronized void onConnectionLost(){
        for (RemoteListener remoteListener : mRemoteListeners) {
            remoteListener.onConnectionLost();
        }
    }

    /**
     * Pass data to remote robot
     * @param uuid
     * @param object
     */
    public void send(String uuid, Object object, final boolean asString) {
        mTransmitUUID = uuid;
        //Send to firebase
        if(asString){
            mFirebaseTransmit.child(uuid).child(CONTROL).child(DATA).setValue(object.toString());
        } else {
            mFirebaseTransmit.child(uuid).child(CONTROL).child(DATA).setValue(object);
        }
        timeStamp();
    }

    void timeStamp(){
        if(mTransmitUUID != null) {
            //Set time stamp
            mFirebaseTransmit.child(mTransmitUUID).child(CONTROL).child(CONNECTED).child(TIMESTAMP).setValue(ServerValue.TIMESTAMP);
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
        //mFirebaseListen.child(CONTROL).addChildEventListener(this);
        //Setup transmitter
        mFirebaseTransmit = new Firebase(DataStore.FIREBASE_URL).child(CONTROL);
        //Listen for controler disconnect
        Firebase connectedRef = new Firebase(DataStore.BASE_FIREBASE_URL + ".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected == false) {
                    onConnectionLost();
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {

            }
        });
        //Listen for remote disconnect
        mFirebaseListen.child(CONTROL).child(CONNECTED).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Get first time stamp
                final Long timeStamp = dataSnapshot.getValue(Long.class);
                if(Constants.LOGGING){
                    Log.d("TimeStamp", "Control Timestamp: " + timeStamp.toString());
                }
                mTimeStamp = timeStamp;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final Long timeStamp = dataSnapshot.getValue(Long.class);
                //Get change in time
                mTimeSinceLastControl = timeStamp - mTimeStamp;
                //Save current time
                mTimeStamp = timeStamp;
                if(Constants.LOGGING){
                    Log.d("TimeStamp", "Lag: " + mTimeSinceLastControl);
                }
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
        });
    }

    public Firebase getControlRef() {
        return mFirebaseTransmit;
    }
}
