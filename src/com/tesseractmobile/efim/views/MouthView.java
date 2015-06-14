package com.tesseractmobile.efim.views;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.tesseractmobile.efim.R;

public class MouthView extends TextView implements OnInitListener{

    final Handler handler = new Handler();
    private final TextToSpeech mTts;
    private boolean isTalkReady;
    private State mState;
    private int mLastAnimation;
    private long mLastChange;
    
    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextColor(Color.rgb(100, 0, 255));
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mTts = new TextToSpeech(context, this);
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            
            @Override
            public void onStart(final String utteranceId) {
                setmState(State.TALKING);
            }
            
            @Override
            public void onError(final String utteranceId) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onDone(final String utteranceId) {
                setmState(State.NOT_TALKING);
            }
        });
        mLastChange = SystemClock.uptimeMillis();
    }

    
    @Override
    protected void onDraw(final Canvas canvas) {
        if(mState == State.TALKING){
            final long uptimeMillis = SystemClock.uptimeMillis();
            if(uptimeMillis - mLastChange > 70){
                mLastChange = uptimeMillis;
                setNextMouth();
            }
            invalidate();
        } else {
            setBackgroundResource(R.drawable.mouth_static);
        }
        super.onDraw(canvas);
    }


    private void setNextMouth() {
        mLastAnimation++;
        if(mLastAnimation > 5){
            mLastAnimation = 0;
        }
        final int image;
        switch(mLastAnimation){
            case 0:
                image = R.drawable.mouth_static;
                break;
            case 1:
                image = R.drawable.mouth_speaking_1;
                break;
            case 2:
                image = R.drawable.mouth_speaking_2;
                break;
            case 3:
                image = R.drawable.mouth_speaking_3;
                break;
            case 4:
                image = R.drawable.mouth_speaking_2;
                break;
            default:
                image = R.drawable.mouth_speaking_1;
                break;
        }
        setBackgroundResource(image);
    }


    @Override
    public void setText(final CharSequence text, final BufferType type) {
        if (handler != null) {
            if(text == null){
                super.setText("Error null text!", type);
                return;
            }
            //Speak if ready
            if(isTalkReady){
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                mTts.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, map);
            }
        }
        super.setText(text, type);
    }

    @Override
    public void onInit(final int status) {
        isTalkReady = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        mTts.stop();
        mTts.shutdown();
        super.onDetachedFromWindow();
    }

    /**
     * @return the mState
     */
    private State getmState() {
        return mState;
    }

    /**
     * @param mState the mState to set
     */
    private void setmState(final State mState) {
        this.mState = mState;
        postInvalidate();
    }

    private enum State {
        TALKING, NOT_TALKING
    }
    
}
