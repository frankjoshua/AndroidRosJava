package com.tesseractmobile.pocketbot.robot;

import android.provider.ContactsContract;

import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;

/**
 * Created by josh on 11/16/2015.
 */
public class Robot extends AIRobot {

    static private RobotInterface mRobot;
    final private DataStore mDataStore;

    private Robot(final DataStore dataStore){
        mDataStore = dataStore;
    };

    static public void init(final DataStore dataStore){
        if(mRobot == null){
            mRobot = new Robot(dataStore);
        }
    }

    static public RobotInterface get(){
        return mRobot;
    }

    @Override
    public void setAuthToken(String robotId, String token) {
        mDataStore.setAuthToken(robotId, token);
    }

    @Override
    public void registerOnAuthCompleteListener(DataStore.OnAuthCompleteListener onAuthCompleteListener) {
        mDataStore.registerOnAuthCompleteListener(onAuthCompleteListener);
    }

    @Override
    public void unregisterOnAuthCompleteListener(DataStore.OnAuthCompleteListener onAuthCompleteListener) {
        mDataStore.unregisterOnAuthCompleteListener(onAuthCompleteListener);
    }

    @Override
    public void deleteRobot(String robotId) {
        mDataStore.deleteRobot(robotId);
    }

    @Override
    public DataStore getDataStore() {
        return mDataStore;
    }
}
