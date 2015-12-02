package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.JoystickView;
import com.tesseractmobile.pocketbot.views.MouthView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFace extends BaseFace implements JoystickView.JoystickListener {

    public static final String JOY_X = "JoyX";
    public static final String JOY_Y = "JoyY";
    public static final String JOY_Z = "JoyZ";
    /** PubNub message delay in millis */
    public static final int PUBNUB_MAX_TRANSMIT_SPEED = 100;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    //Channel that the remote robot is listening on
    private String mChannel;
    private long mLastUpdate = SystemClock.uptimeMillis();

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ControlFace.this.handleMessage(msg);
        }
    };

    private TextView mFaceData;
    private float x, y, z;

    public ControlFace(final View view){
        numberFormat.setMinimumFractionDigits(2);
        mFaceData = (TextView) view.findViewById(R.id.tvFaceData);
        ((JoystickView) view.findViewById(R.id.joyStick)).setJoystickListener(this);
    }

    private void handleMessage(Message msg) {
        final String data =  "JoyX: " + numberFormat.format(x) + " JoyY: " + numberFormat.format(y);// + " JoyZ: " + numberFormat.format(z);
        mFaceData.setText(data);
    }

    @Override
    public void setEmotion(Emotion emotion) {

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
        updateRemote(false);
    }

    /**
     * Send control data to PubNub
     * @param force true if data must be sent
     */
    private void updateRemote(boolean force) {
        if(force || SystemClock.uptimeMillis() - mLastUpdate > PUBNUB_MAX_TRANSMIT_SPEED){
            mLastUpdate = SystemClock.uptimeMillis();
            final JSONObject json = new JSONObject();
            try {
                json.put(JOY_X, x);
                json.put(JOY_Y, y);
                json.put(JOY_Z, z);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Send to the remote robot
            if(mChannel != null){
                RemoteControl.get().send(mChannel, json);
            }
        }
    }

    @Override
    public void onFocusChange(boolean hasFocus) {
        //Send message when user lets go of controls
        if(hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick(0, 0, 0);
            mRobotInterface.sendSensorData(true);
            this.x = sensorData.getJoyX();
            this.y = sensorData.getJoyY();
            this.z = sensorData.getJoyZ();
            mHandler.sendEmptyMessage(0);
            updateRemote(true);
        }
    }

    public void setChannel(final String channel){
        mChannel = channel;
    }
}
