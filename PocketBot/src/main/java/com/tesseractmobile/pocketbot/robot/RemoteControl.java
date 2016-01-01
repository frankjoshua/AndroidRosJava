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
public class RemoteControl implements ChildEventListener {
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
            //Stop listening to the old channel
           // pubnub.unsubscribe(this.id);
            //Stop listening for firebase messages
            mFirebaseListen.removeEventListener(this);
        }
        //Set the ID
        this.id = id;
        //Listen for messages from firebase
        mFirebaseListen = new Firebase("https://boiling-torch-4457.firebaseio.com/").child(CONTROL).child(id);
        mFirebaseListen.child(CONTROL).addChildEventListener(this);
        mFirebaseTransmit = new Firebase("https://boiling-torch-4457.firebaseio.com/").child(CONTROL);
        //Listen for messages from pubnub
//        try {
//            pubnub.subscribe(id, new Callback() {
//                @Override
//                public void errorCallback(String channel, PubnubError error) {
//                    Log.e("PUBNUB", error.getErrorString());
//                }
//
//                @Override
//                public void successCallback(String channel, final Object message, String timeToken) {
//                    //Update listeners
//                    final long timeElapsed = timeElapsed(timeToken);
//                    if(timeElapsed < 500) {
//                        onObjectReceived(message);
//                    } else {
//                        Log.e("PUBNUB", "Old Packet " + Long.toString(timeElapsed));
//                    }
//                    //Log.d("PUBNUB", Long.toString(timeElapsed(timeToken)));
//                }
//            });
//        } catch (PubnubException e) {
//            e.printStackTrace();
//        }
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

    private long timeElapsed(final String timeToken) {
        final Date now = new Date();
        final Date then = new Date(Long.valueOf(timeToken) / 10000);
        //Log.d("PUBNUB", "Now " + Long.toString(now.getTime()));
        //Log.d("PUBNUB", "Then " + Long.toString(then.getTime()));
        return now.getTime() - then.getTime();
    }

    /**
     * Pass data to remote robot
     * @param channel
     * @param json
     */
    public void send(String channel, JSONObject json, final boolean required) {
        //Send to PubNub
//        pubnub.publish(channel, json, required, new Callback() {
//            @Override
//            public void successCallback(String channel, Object message, String timetoken) {
//
//            }
//        });
        //Send to firebase
        mFirebaseTransmit.child(channel).child("control").child(DATA).setValue(json.toString());
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        onObjectReceived(dataSnapshot.getValue(JSONObject.class));
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


}
