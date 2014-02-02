package com.tesseractmobile.efim;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;

import com.tesseractmobile.efim.UsbConnectionService.LocalBinder;
import com.tesseractmobile.efim.robot.RobotCommand;
import com.tesseractmobile.efim.robot.RobotCommand.RobotCommandType;
import com.tesseractmobile.efim.robot.RobotCommandInterface;
import com.tesseractmobile.efim.robot.RobotEvent;
import com.tesseractmobile.efim.robot.RobotEventListener;
import com.tesseractmobile.efim.views.MouthView;

public class UsbAccessoryActivity extends Activity implements OnClickListener, RobotEventListener {

    private MouthView               textView;

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

        setContentView(R.layout.robot_face);

        textView = (MouthView) findViewById(R.id.mouthView);

        findViewById(R.id.eyeViewLeft).setOnClickListener(this);
        findViewById(R.id.eyeViewRight).setOnClickListener(this);
        findViewById(R.id.mouthView).setOnClickListener(this);
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
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            textView.setText("Ouch!!");
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
            finish();
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
                    textView.setText(robotEvent.getMessage());
                    break;
                case DISCONNECT:
                    finish();
                    break;
                }
            }
        };
        handler.post(runnable);
        
    }

}
