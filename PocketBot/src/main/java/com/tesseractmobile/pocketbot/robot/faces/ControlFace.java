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
    public static final int PUBNUB_MAX_TRANSMIT_SPEED = 200;
    private final TextView mInputTextView;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    //Channel that the remote robot is listening on
    private String mChannel;
    private long mLastUpdate = SystemClock.uptimeMillis();
    private String inputText;

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ControlFace.this.handleMessage(msg);
        }
    };

    private TextView mFaceData;
    private float x, y, z;
    private int mDestHeading;
    private MouthView.SpeechCompleteListener mSpeechCompleteListener;

    public ControlFace(final View view){
        numberFormat.setMinimumFractionDigits(2);
        mFaceData = (TextView) view.findViewById(R.id.tvFaceData);
        ((JoystickView) view.findViewById(R.id.joyStick)).setJoystickListener(this);
        ((JoystickView) view.findViewById(R.id.joyStickLeft)).setJoystickListener(this);
        mInputTextView = (TextView) view.findViewById(R.id.tvInput);
    }

    private void handleMessage(Message msg) {
        final String data =  "Heading: " + mDestHeading + " JoyX: " + numberFormat.format(x) + " JoyY: " + numberFormat.format(y);// + " JoyZ: " + numberFormat.format(z);
        mFaceData.setText(data);
        if(inputText != null){
            mInputTextView.setText(inputText);
            if(mSpeechCompleteListener != null){
                mSpeechCompleteListener.onSpeechComplete();
            }
        }
    }

    @Override
    public void setEmotion(Emotion emotion) {

    }

    @Override
    public void look(float x, float y, float z) {

    }

    @Override
    public void say(final String text) {
        inputText = text;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void setOnSpeechCompleteListener(MouthView.SpeechCompleteListener speechCompleteListener) {
        mSpeechCompleteListener = speechCompleteListener;
    }

    @Override
    public void onPositionChange(final JoystickView joystickView, float x, float y, float z) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        if(joystickView.getId() == R.id.joyStick) {
            sensorData.setJoystick(x, y, z);
            this.x = sensorData.getJoyX();
            this.y = sensorData.getJoyY();
            this.z = sensorData.getJoyZ();
        }

        if(joystickView.getId() == R.id.joyStickLeft) {
            final double degrees;
            if(x != 0 && y != 0) {
                final double heading = Math.atan2(x, y) * 57;
                degrees = heading < 0 ? heading + 360 : heading;
            } else {
                degrees = 0;
            }
            sensorData.setDestHeading((int) Math.round(degrees));
            mDestHeading =  sensorData.getDestHeading();
        }

        mRobotInterface.sendSensorData(false);
        updateRemote(false);
        mHandler.sendEmptyMessage(0);
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
                RemoteControl.get().send(mChannel, json, force);
            }
        }
    }

    @Override
    public void onFocusChange(final JoystickView joystickView, boolean hasFocus) {
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
