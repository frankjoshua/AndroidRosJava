package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.MouthView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

/**
 * Created by josh on 10/31/2015.
 */
public class TelePresenceFace extends BaseFace {

    private TextView mUserId;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private Handler mHandler = new Handler();

    public TelePresenceFace(View view) {
        numberFormat.setMinimumFractionDigits(2);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
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

    public void sendJson(JSONObject jsonObject) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        try {
            final float x = (float) jsonObject.getDouble(ControlFace.JOY_X);
            final float y = (float) jsonObject.getDouble(ControlFace.JOY_Y);
            final float z = (float) jsonObject.getDouble(ControlFace.JOY_Z);
            sensorData.setJoystick(x, y, z);
            mRobotInterface.sendSensorData(false);
            //Show joystick data in text view
            final String data =  "JoyX: " + numberFormat.format(x) + " JoyY: " + numberFormat.format(y);// + " JoyZ: " + numberFormat.format(z);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUserId.setText(data);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
