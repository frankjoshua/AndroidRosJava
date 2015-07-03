package com.tesseractmobile.efim.views;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.tesseractmobile.efim.R;

public class MouthView extends TextView implements OnInitListener, OnDataCaptureListener{

    final Handler handler = new Handler();
    private final TextToSpeech mTts;
    private boolean isTalkReady;
    private State mState;
    private int mLastAnimation;
    private final long mLastChange;
    private final Path mPath;
    private final Paint mPaint;
    private final Path mWavePath;
    
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
        
        mPath = new Path();
        mWavePath = new Path();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Style.STROKE);
        

        final Visualizer mVisualizer = new Visualizer(0);
        mVisualizer.setDataCaptureListener(MouthView.this, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
    }

    
    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        

        canvas.drawPath(mPath, mPaint);
        //canvas.drawPath(mWavePath, mPaint);
//        if(mState == State.TALKING){
//            final long uptimeMillis = SystemClock.uptimeMillis();
//            if(uptimeMillis - mLastChange > 70){
//                mLastChange = uptimeMillis;
//                setNextMouth();
//            }
//            invalidate();
//        } else {
//            setBackgroundResource(R.drawable.mouth_static);
//        }
        //Draw text
        super.onDraw(canvas);
    }


    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawMouth(w, h);
    }


    /**
     * @param w
     * @param h
     */
    protected void drawMouth(final int w, final int h) {
        mPath.reset();
        mPath.moveTo(0, h / 2);
        //mPath.lineTo(w, h / 2);
//        mPath.quadTo(w / 2, h, w, h/2);
//        //mPath.lineTo(0, h / 2);
//        mPath.quadTo(w / 2, (float) (h *.75), 0, h/2);
        
        //Draw vertical lines
        for(int line = 1; line < 5; line++){
            mPath.moveTo((w / 5) * line, 0);
            mPath.lineTo((w / 5) * line, h);
        }
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

    @Override
    public void onWaveFormDataCapture(final Visualizer visualizer, final byte[] waveform, final int samplingRate) {
        //Log.d("wave", Arrays.toString(waveform));
        drawMouth(getWidth(), getHeight());
        final int waveWidth = getWidth() / 10;
        final int centerY = getHeight() / 2;
        mPath.moveTo(0, centerY);
        int x = 0;
        int sum = 0;
        int count = 0;
        for(int i = 0; i < waveform.length && x < getWidth(); i ++){
            count++;
            final byte wave = waveform[i];
            sum += wave;
            if(i % 100 == 0){
                //mPath.lineTo(x, getHeight() / 2 + wave);
                final int waveHeight = sum / count;
                mPath.quadTo(x + waveWidth / 2, centerY + waveHeight * 2, x + waveWidth, centerY);
                x += waveWidth;
                count = 0;
                sum = 0;
            }
            
        }
        invalidate();
    }


    @Override
    public void onFftDataCapture(final Visualizer visualizer, final byte[] fft, final int samplingRate) {
        // TODO Auto-generated method stub
        
    }
    
}
