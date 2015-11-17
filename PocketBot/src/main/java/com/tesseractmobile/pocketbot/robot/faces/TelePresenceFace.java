package com.tesseractmobile.pocketbot.robot.faces;

import android.view.View;

import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.MouthView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by josh on 10/31/2015.
 */
public class TelePresenceFace extends BaseFace {

    public TelePresenceFace(View view) {

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
