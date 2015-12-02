package com.tesseractmobile.pocketbot.robot;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by josh on 12/1/2015.
 */
public class RemoteControl {
    private Pubnub pubnub = new Pubnub("pub-c-2bd62a71-0bf0-4d53-bf23-298fd6b34c3e", "sub-c-75cdf46e-83e9-11e5-8495-02ee2ddab7fe");

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
            pubnub.unsubscribe(this.id);
        }
        //Set the ID
        this.id = id;
        //Listen for messages from pubnub
        try {
            pubnub.subscribe(id, new Callback() {
                @Override
                public void errorCallback(String channel, PubnubError error) {
                    throw new UnsupportedOperationException(error.getErrorString());
                }

                @Override
                public void successCallback(String channel, final Object message, String timetoken) {
                    //Update listeners
                    for(RemoteListener remoteListener : mRemoteListeners){
                        remoteListener.onMessageReceived(message);
                    }
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pass data to remote robot
     * @param channel
     * @param json
     */
    public void send(String channel, JSONObject json) {
        pubnub.publish(channel, json, false, new Callback() {
            @Override
            public void successCallback(String channel, Object message, String timetoken) {

            }
        });
    }
}
