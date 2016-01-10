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

    public static final String JOY1_X = "Joy1X";
    public static final String JOY1_Y = "Joy1Y";
    public static final String JOY1_Z = "Joy1Z";
    public static final String JOY1_A = "Joy1A";
    public static final String JOY1_B = "Joy1B";
    public static final String JOY1_HEADING = "Heading1";
    public static final String JOY2_X = "Joy2X";
    public static final String JOY2_Y = "Joy2Y";
    public static final String JOY2_Z = "Joy2Z";
    public static final String JOY2_A = "Joy2A";
    public static final String JOY2_B = "Joy2B";
    public static final String JOY2_HEADING = "Heading2";
    public static final String BATTERY = "Battery";

    /** remote message delay in millis */
    public static final int REMOTE_MAX_TRANSMIT_SPEED = 100;

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
    private float mBattery;

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
        say("Battery " + Integer.toString(sensorData.getSensor().battery) + "%");
        if(joystickView.getId() == R.id.joyStick) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick1(x, y, z, a, b, heading);
            //Valus from sensor data are rounded so use those
            mJoy1.X = sensorData.getControl().joy1.X;
            mJoy1.Y = sensorData.getControl().joy1.Y;
            mJoy1.Z = sensorData.getControl().joy1.Z;
            mJoy1.A = sensorData.getControl().joy1.A;
            mJoy1.B = sensorData.getControl().joy1.B;
            mJoy1.heading = sensorData.getControl().joy1.heading;
        }

        if(joystickView.getId() == R.id.joyStickLeft) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick2(x, y, z, a, b, heading);
            //Valus from sensor data are rounded so use those
            mJoy2.X = sensorData.getControl().joy2.X;
            mJoy2.Y = sensorData.getControl().joy2.Y;
            mJoy2.Z = sensorData.getControl().joy2.Z;
            mJoy2.A = sensorData.getControl().joy2.A;
            mJoy2.B = sensorData.getControl().joy2.B;
            mJoy2.heading = sensorData.getControl().joy2.heading;
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
        if(force || SystemClock.uptimeMillis() - mLastUpdate > REMOTE_MAX_TRANSMIT_SPEED){
            //Make sure channel is set
            String channel = this.mChannel;
            if(channel != null){
                //Mark the time
                mLastUpdate = SystemClock.uptimeMillis();
                //Create a json object to send
                final JSONObject json = new JSONObject();
                try {
                    //Convert floats to strings so they don't get cast as doubles
                    json.put(JOY1_X, mJoy1.X);
                    json.put(JOY1_Y, mJoy1.Y);
                    json.put(JOY1_Z, mJoy1.Z);
                    json.put(JOY1_A, mJoy1.A);
                    json.put(JOY1_B, mJoy1.B);
                    json.put(JOY1_HEADING, mJoy1.heading);
                    json.put(JOY2_X, mJoy2.X);
                    json.put(JOY2_Y, mJoy2.Y);
                    json.put(JOY2_Z, mJoy2.Z);
                    json.put(JOY2_A, mJoy2.A);
                    json.put(JOY2_B, mJoy2.B);
                    json.put(JOY2_HEADING, mJoy1.heading);
                    //Add battery information
                    final SensorData sensorData = mRobotInterface.getSensorData();
                    json.put(BATTERY, sensorData.getSensor().battery);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Send to the remote robot
                RemoteControl.get().send(channel, json, true);
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
            sensorData.setJoystick2(0, 0, 0);
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
