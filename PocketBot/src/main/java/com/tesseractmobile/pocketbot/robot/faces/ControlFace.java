package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.BaseFaceActivity;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.JoystickView;
import com.tesseractmobile.pocketbot.views.MouthView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFace extends BaseFace implements JoystickView.JoystickListener {

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ControlFace.this.handleMessage(msg);
        }
    };

    private TextView mFaceData;
    private float x, y, z;

    public ControlFace(final View view){
        mFaceData = (TextView) view.findViewById(R.id.tvFaceData);
        ((JoystickView) view.findViewById(R.id.joyStick)).setJoystickListener(this);
    }

    private void handleMessage(Message msg) {
        mFaceData.setText(Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z));
    }

    @Override
    public void setEmotion(BaseFaceActivity.Emotion emotion) {

    }

    @Override
    public void look(float x, float y, float z) {

    }

    @Override
    public void say(String text) {

    }

    @Override
    public void setOnSpeechCompleteListener(MouthView.SpeechCompleteListener speechCompleteListener) {

    }

    @Override
    public void onPositionChange(float x, float y, float z) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        sensorData.setJoystick(x, y, z);
        mRobotInterface.sendSensorData(false);
        this.x = sensorData.getJoyX();
        this.y = sensorData.getJoyY();
        this.z = sensorData.getJoyZ();
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onFocusChange(boolean hasFocus) {
        //Send message when user lets go of controls
        if(hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick(0, 0, 0);
            mRobotInterface.sendSensorData(false);
            this.x = sensorData.getJoyX();
            this.y = sensorData.getJoyY();
            this.z = sensorData.getJoyZ();
            mHandler.sendEmptyMessage(0);
        }
    }


}
