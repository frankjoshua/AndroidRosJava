package com.tesseractmobile.pocketbot.activities;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.firebase.client.Firebase;
import com.quickblox.core.QBSettings;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.crashlytics.android.Crashlytics;
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
        final String robotId = PocketBotSettings.getRobotId(this);
        //Init Robot
        Robot.init();
        //Setup Quickblox
        QBSettings.getInstance().fastConfigInit("30377", "XOF58dzCGkyg8a9", "NZa9WcFAmhmrKr8");
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

        //Start Firebase - before DataStore
        Firebase.setAndroidContext(this);

        //Init DataStore
        DataStore.init(this);

        //Start up remote control service
        RemoteControl.init(this, robotId);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //Enable MultiDex support
        //MultiDex.install(this);
    }

}
