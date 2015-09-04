package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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


    private boolean flipFlop;
    
    @Override
    public void onClick(final View v) {
        super.onClick(v);
        
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
//            if(flipFlop){
//                final RobotCommand robotCommand = new RobotCommand(RobotCommandType.NOD);
//                robotCommandInterface.sendCommand(robotCommand);
//            } else {
//                final RobotCommand robotCommand = new RobotCommand(RobotCommandType.SHAKE);
//                robotCommandInterface.sendCommand(robotCommand);
//            }
            flipFlop = !flipFlop;

            break;
        case R.id.eyeViewRight:
            //robotCommandInterface.reconnectRobot();
            break;
        case R.id.mouthView:
            super.onClick(v);
            break;
        }

    }


}
