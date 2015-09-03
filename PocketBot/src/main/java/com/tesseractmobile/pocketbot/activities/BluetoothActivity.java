package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.service.BluetoothService;

/**
 * Created by josh on 9/1/2015.
 */
public class BluetoothActivity extends AiActivity{

    private ServiceConnection bluetoothServiceConnection;
    private BluetoothService mBlueToothService;

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to bluetooth service
        bluetoothServiceConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBlueToothService = ((BluetoothService.LocalBinder) service).getService();
                mBlueToothService.registerBodyConnectionListener(BluetoothActivity.this);
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
        //Unbind from bluetooth service
        unbindService(bluetoothServiceConnection);
        bluetoothServiceConnection = null;
    }

}
