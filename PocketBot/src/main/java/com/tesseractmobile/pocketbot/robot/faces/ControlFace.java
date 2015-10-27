package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.BaseFaceActivity;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.JoystickView;
import com.tesseractmobile.pocketbot.views.MouthView;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFace extends BaseFace implements RobotFace, JoystickView.JoystickListener {

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ControlFace.this.handleMessage(msg);
        }
    };

    final private TextView mFaceData;
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
//        if(x == 1.0f){
//            throw new UnsupportedOperationException();
//        }
        this.x = x;
        this.y = y;
        this.z = z;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void say(String text) {

    }

    @Override
    public void setOnSpeechCompleteListener(MouthView.SpeechCompleteListener speechCompleteListener) {

    }

    @Override
    public void onPositionChange(float x, float y, float z) {
        //mRobotInterface.look(x, y, z);
        final SensorData sensorData = mRobotInterface.getSensorData();
        sensorData.setJoystick(x, y, z);
    }

    @Override
    public void onFocusChange(boolean hasFocus) {
        if(hasFocus){
            mRobotInterface.humanSpotted(0);
        } else {
            mRobotInterface.humanSpotted(SensorData.NO_FACE);
        }
    }
}
