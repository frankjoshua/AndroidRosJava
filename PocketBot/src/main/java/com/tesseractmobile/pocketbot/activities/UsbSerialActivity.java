package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.tesseractmobile.pocketbot.service.BodyService;
import com.tesseractmobile.pocketbot.service.UsbSerialService;

/**
 * Created by josh on 11/18/2015.
 */
public class UsbSerialActivity extends AiActivity{
    private final ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(final ComponentName name) {

        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final BodyService bodyService = ((BodyService.LocalBinder) service).getService();
            bodyService.registerBodyConnectionListener(getRobotInterface().getBodyConnectionListener());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Speed up sensor data
        setSensorDelay(50);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent bindIntent = new Intent(this, UsbSerialService.class);
        if(bindService(bindIntent, conn, Service.BIND_AUTO_CREATE) == false){
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected void onStop() {
        unbindService(conn);
        super.onStop();
    }
}
