package com.tesseractmobile.efim.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import com.tesseractmobile.efim.R;
import com.tesseractmobile.efim.UsbConnectionService;
import com.tesseractmobile.efim.UsbConnectionService.LocalBinder;
import com.tesseractmobile.efim.robot.RobotCommand;
import com.tesseractmobile.efim.robot.RobotCommand.RobotCommandType;
import com.tesseractmobile.efim.robot.RobotCommandInterface;
import com.tesseractmobile.efim.robot.RobotEvent;
import com.tesseractmobile.efim.robot.RobotEventListener;

public class UsbAccessoryActivity extends BaseFaceActivity implements RobotEventListener {

    

    private RobotCommandInterface   robotCommandInterface;
    private final ServiceConnection conn = new ServiceConnection() {

                                             @Override
                                             public void onServiceDisconnected(final ComponentName name) {
                                                 robotCommandInterface.unregisterRobotEventListener(UsbAccessoryActivity.this);
                                                 robotCommandInterface = null;
                                             }

                                             @Override
                                             public void onServiceConnected(final ComponentName name, final IBinder service) {
                                                 robotCommandInterface = ((LocalBinder) service).getService();
                                                 robotCommandInterface.registerRobotEventListener(UsbAccessoryActivity.this);
                                             }
                                         };
    
    private final Handler handler = new Handler();
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "USB connection detected", Toast.LENGTH_LONG).show();
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

    private boolean flipFlop;
    
    @Override
    public void onClick(final View v) {
        //super.onClick(v);
        
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            if(flipFlop){
                final RobotCommand robotCommand = new RobotCommand(RobotCommandType.NOD);
                robotCommandInterface.sendCommand(robotCommand);
            } else {
                final RobotCommand robotCommand = new RobotCommand(RobotCommandType.SHAKE);
                robotCommandInterface.sendCommand(robotCommand);
            }
            flipFlop = !flipFlop;

            break;
        case R.id.eyeViewRight:
            robotCommandInterface.reconnectRobot();
            break;
        case R.id.mouthView:
            super.onClick(v);
            break;
        }

    }

    @Override
    public void onRobotEvent(final RobotEvent robotEvent) {
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                switch (robotEvent.getEventType()) {
                case ERROR:
                    say(robotEvent.getMessage());
                    break;
                case DISCONNECT:
                    say("Disconnect command recieved");
                    handler.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                    break;
                }
            }
        };
        handler.post(runnable);
        
    }

}
