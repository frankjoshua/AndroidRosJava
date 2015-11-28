package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by josh on 10/17/2015.
 */
public class EfimFace extends BaseFace implements RobotFace, OnClickListener{

    private MouthView mouthView;
    private EyeView mLeftEye;
    private EyeView mRightEye;

    private Emotion mEmotion = Emotion.JOY;

    private String mTempText;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mouthView.setText(mTempText);
        }
    };

    public EfimFace(final View view){

        //Init views
        mouthView = (MouthView) view.findViewById(R.id.mouthView);
        mLeftEye = (EyeView) view.findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) view.findViewById(R.id.eyeViewRight);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);
    }

    @Override
    public void look(float x, float y, float z) {
        mLeftEye.look(x, y);
        mRightEye.look(x, y);
    }

    @Override
    public void say(final String text) {
         mTempText = text;
         mHandler.sendEmptyMessage(0);
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
                mRobotInterface.say("Ouch");
                fear();
                // finish();
                break;
            case R.id.eyeViewRight:
                //say("I'm going to kill you in my sleep... Oh wait, your sleep");
                mRobotInterface.say("Please don't poke my eye.");
                anger();
                break;
            case R.id.mouthView:
                mRobotInterface.listen();
                break;
        }
    }

    @Override
    public void setEmotion(final Emotion emotion) {
        if (mEmotion != emotion) {
            mEmotion = emotion;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    switch (emotion) {
                        case ACCEPTED:
                            mLeftEye.squint();
                            mRightEye.squint();
                            mouthView.smile();
                            break;
                        case SUPRISED:
                            mLeftEye.open();
                            mRightEye.open();
                            mLeftEye.blink();
                            mRightEye.blink();
                            mouthView.smile();
                            break;
                        case AWARE:
                            mLeftEye.open();
                            mRightEye.squint();
                            mouthView.nuetral();
                            break;
                        case JOY:
                            mLeftEye.wideOpenLeft();
                            mRightEye.wideOpenRight();
                            mouthView.smile();
                            break;
                        case FEAR:
                            fear();
                            mouthView.frown();
                            break;
                        case ANGER:
                            anger();
                            mouthView.frown();
                            break;
                        default:
                            mLeftEye.squint();
                            mRightEye.squint();
                            mouthView.frown();
                            say("I don't under stand the emotion " + emotion + ".");
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

}
