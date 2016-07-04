package com.tesseractmobile.pocketbot.activities;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.quickblox.core.QBSettings;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;

import io.fabric.sdk.android.Fabric;

/**
 * Created by josh on 9/27/2015.
 */
public class PocketBotApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //Track errors
        Fabric.with(this, new Crashlytics());
        //Get robot id first so Shared preference listeners don't trigger
        final String robotId = PreferenceManager.getDefaultSharedPreferences(this).getString(PocketBotSettings.KEY_ROBOT_ID, PocketBotSettings.ROBOT_ID_NOT_SET);
        //Init DataStore
        final DataStore dataStore = DataStore.init(this);
        //Start up remote control service
        RemoteControl.init(this, dataStore, robotId);
        //Init Robot
        Robot.init(dataStore);
        Robot.get().setIsNew(robotId.equals(PocketBotSettings.ROBOT_ID_NOT_SET));
        //Setup Quickblox
        QBSettings.getInstance().init(this, "30377", "XOF58dzCGkyg8a9", "NZa9WcFAmhmrKr8");
        QBSettings.getInstance().setAccountKey("dRPuNzPqTxT3o4pi6syS");
        //Bind to voice recognition service to hold constant connection
        final ServiceConnection voiceRecognitionServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                VoiceRecognitionService voiceRecognitionService = ((VoiceRecognitionService.LocalBinder) service).getService();
                voiceRecognitionService.registerVoiceRecognitionListener(Robot.get().getVoiceRecognitionListener());
                Robot.get().setVoiceRecognitionService(voiceRecognitionService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        final Intent bindIntent = new Intent(this, VoiceRecognitionService.class);
        if (bindService(bindIntent, voiceRecognitionServiceConnection, Service.BIND_AUTO_CREATE) == false) {
            throw new UnsupportedOperationException("Error binding to service");
        }


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //Enable MultiDex support
        MultiDex.install(this);
    }

}
