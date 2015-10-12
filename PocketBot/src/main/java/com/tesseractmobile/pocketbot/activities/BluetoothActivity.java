package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.service.BluetoothService;
import com.tesseractmobile.pocketbot.service.BodyService;

/**
 * Created by josh on 9/1/2015.
 */
public class BluetoothActivity extends AiActivity{

    private ServiceConnection bluetoothServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Only phones with API 18+ have Bluetooth LE
        if(Build.VERSION.SDK_INT < 18){
            startActivity(new Intent(this, AiActivity.class));
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Only phones with API 18+ have Bluetooth LE
        if(Build.VERSION.SDK_INT < 18){
            return;
        }
        //Bind to bluetooth service
        bluetoothServiceConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final BodyService blueToothService = ((BodyService.LocalBinder) service).getService();
                blueToothService.registerBodyConnectionListener(BluetoothActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        final Intent bluetoothBindIntent = new Intent(this, BluetoothService.class);
        if(bindService(bluetoothBindIntent, bluetoothServiceConnection, Service.BIND_AUTO_CREATE) == false){
            throw new UnsupportedOperationException("Error binding to bluetooth service");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Only phones with API 18+ have Bluetooth LE
        if(Build.VERSION.SDK_INT < 18){
            return;
        }
        //Unbind from bluetooth service
        unbindService(bluetoothServiceConnection);
        bluetoothServiceConnection = null;
    }

}
