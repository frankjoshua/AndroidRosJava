package com.tesseractmobile.pocketbot.robot.faces;

import android.app.Activity;
import android.content.Context;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.BaseFaceActivity;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by josh on 10/17/2015.
 */
public class EfimFace implements RobotFace, OnClickListener, SpeechCompleteListener{

    private MouthView mouthView;
    private EyeView mLeftEye;
    private EyeView mRightEye;

    private BaseFaceActivity.Emotion mEmotion = BaseFaceActivity.Emotion.JOY;

    private Handler mHandler = new Handler();

    private RobotInterface mRobotInterface;

    public EfimFace(final View view){

        //Init views
        mouthView = (MouthView) view.findViewById(R.id.mouthView);
        mLeftEye = (EyeView) view.findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) view.findViewById(R.id.eyeViewRight);

        //Listen for end of speech
        mouthView.setOnSpeechCompleteListener(this);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);
    }

    @Override
    public void setRobotInterface(final RobotInterface robotInterface){
        this.mRobotInterface = robotInterface;
    }

    @Override
    public void look(float x, float y, float z) {
        mLeftEye.look(x, y);
        mRightEye.look(x, y);
        if (z > .55f) {
            setEmotion(BaseFaceActivity.Emotion.FEAR);
        }
    }

    @Override
    public void say(String text) {
         mouthView.setText(text);
    }

    @Override
    public void setOnSpeechCompleteListener(SpeechCompleteListener speechCompleteListener) {
        mouthView.setOnSpeechCompleteListener(speechCompleteListener);
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
            case R.id.eyeViewLeft:
                say("Ouch");
                fear();
                // finish();
                break;
            case R.id.eyeViewRight:
                //say("I'm going to kill you in my sleep... Oh wait, your sleep");
                say("Please don't poke my eye.");
                anger();
                break;
            case R.id.mouthView:
                mRobotInterface.listen();
                break;
        }
    }

    @Override
    public void setEmotion(final BaseFaceActivity.Emotion emotion) {
        if (mEmotion != emotion) {
            mEmotion = emotion;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    switch (emotion) {
                        case ACCEPTED:
                            mLeftEye.squint();
                            mRightEye.squint();
                            break;
                        case SUPRISED:
                            mLeftEye.open();
                            mRightEye.open();
                            mLeftEye.blink();
                            mRightEye.blink();
                            break;
                        case AWARE:
                            mLeftEye.open();
                            mRightEye.squint();
                            break;
                        case JOY:
                            mLeftEye.wideOpenLeft();
                            mRightEye.wideOpenRight();
                            break;
                        case FEAR:
                            fear();
                            break;
                        case ANGER:
                            anger();
                            break;
                        default:
                            mLeftEye.squint();
                            mRightEye.squint();
                            //say("I don't under stand the emotion " + emotion + ".");
                            break;
                    }
                }
            });
        }
    }

    /**
     * Set look to fearful
     */
    private void fear() {
        mLeftEye.squintLeft();
        mRightEye.squintRight();
    }

    /**
     * Set look to angry
     */
    private void anger() {
        mLeftEye.squintRight();
        mRightEye.squintLeft();
    }

    @Override
    public void onSpeechComplete() {

    }
}
