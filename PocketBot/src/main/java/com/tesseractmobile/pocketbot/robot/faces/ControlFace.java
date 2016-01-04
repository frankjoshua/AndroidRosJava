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
    public static final String JOY_A = "JoyA";
    public static final String JOY_B = "JoyB";
    public static final String HEADING = "Heading";

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
    private SensorData.Joystick mJoy1 = new SensorData.Joystick();
    private SensorData.Joystick mJoy2 = new SensorData.Joystick();
    private MouthView.SpeechCompleteListener mSpeechCompleteListener;

    public ControlFace(final View view){
        numberFormat.setMinimumFractionDigits(2);
        mFaceData = (TextView) view.findViewById(R.id.tvFaceData);
        ((JoystickView) view.findViewById(R.id.joyStick)).setJoystickListener(this);
        ((JoystickView) view.findViewById(R.id.joyStickLeft)).setJoystickListener(this);
        mInputTextView = (TextView) view.findViewById(R.id.tvInput);
    }

    private void handleMessage(Message msg) {
        final String data =  "Heading: " + mJoy2.heading + " JoyX: " + numberFormat.format(mJoy1.X) + " JoyY: " + numberFormat.format(mJoy1.Y);// + " JoyZ: " + numberFormat.format(z);
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
    public void onPositionChange(final JoystickView joystickView, float x, float y, float z, final boolean a, final boolean b) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        if(joystickView.getId() == R.id.joyStick) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick1(x, y, z, a, b, heading);
            mJoy1.X = x;
            mJoy1.Y = y;
            mJoy1.Z = z;
            mJoy1.A = a;
            mJoy1.B = b;
            mJoy1.heading = heading;
        }

        if(joystickView.getId() == R.id.joyStickLeft) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick1(x, y, z, a, b, heading);
            mJoy2.X = x;
            mJoy2.Y = y;
            mJoy2.Z = z;
            mJoy2.A = a;
            mJoy2.B = b;
            mJoy2.heading = heading;
        }

        mRobotInterface.sendSensorData(false);
        updateRemote(false);
        mHandler.sendEmptyMessage(0);
    }

    private int headingFromPosition(float x, float y) {
        final double degrees;
        if(x != 0 && y != 0) {
            final double heading = Math.atan2(x, y) * 57;
            degrees = heading < 0 ? heading + 360 : heading;
        } else {
            degrees = 0;
        }
        return (int) Math.round(degrees);
    }

    /**
     * Send control data to remote robot
     * @param force true if data must be sent
     */
    private void updateRemote(boolean force) {
        if(force || SystemClock.uptimeMillis() - mLastUpdate > PUBNUB_MAX_TRANSMIT_SPEED){
            mLastUpdate = SystemClock.uptimeMillis();
            final JSONObject json = new JSONObject();
            try {
                json.put(JOY_X, mJoy1.X);
                json.put(JOY_Y, mJoy1.Y);
                json.put(JOY_Z, mJoy1.Z);
                json.put(JOY_A, mJoy1.A);
                json.put(JOY_B, mJoy1.B);
                json.put(HEADING, mJoy1.heading);
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
        if(joystickView.getId() == R.id.joyStick && hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick1(0, 0, 0);
            mRobotInterface.sendSensorData(true);
            mJoy1.X = 0;
            mJoy1.Y = 0;
            mJoy1.Z = 0;
            mHandler.sendEmptyMessage(0);
            updateRemote(true);
        }
        if(joystickView.getId() == R.id.joyStickLeft && hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick1(0, 0, 0);
            mRobotInterface.sendSensorData(true);
            mJoy2.X = 0;
            mJoy2.Y = 0;
            mJoy2.Z = 0;
            mHandler.sendEmptyMessage(0);
            updateRemote(true);
        }
    }

    public void setChannel(final String channel){
        mChannel = channel;
    }
}
