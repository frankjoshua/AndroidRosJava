package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.service.BodyService;
import com.tesseractmobile.pocketbot.service.UsbConnectionService;

public class UsbAccessoryActivity extends AiActivity {

    private final ServiceConnection conn = new ServiceConnection() {

                                             @Override
                                             public void onServiceDisconnected(final ComponentName name) {

                                             }

                                             @Override
                                             public void onServiceConnected(final ComponentName name, final IBinder service) {
                                                 final BodyService bodyService = ((BodyService.LocalBinder) service).getService();
                                                 bodyService.registerBodyConnectionListener(UsbAccessoryActivity.this);
                                                 //robotCommandInterface.reconnectRobot();
                                             }
                                         };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Speed up sensor data
        setSensorDelay(120);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent bindIntent = new Intent(this, UsbConnectionService.class);
        bindService(bindIntent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(conn);
        super.onStop();
    }


}
