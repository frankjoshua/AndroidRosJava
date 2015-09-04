package com.tesseractmobile.pocketbot.views;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
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

import com.tesseractmobile.pocketbot.R;

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
    private byte[] mWave = new byte[1024];
    private final Paint mMouthPaint;
    private SpeechCompleteListener mSpeechCompleteListener;

    private HashMap<String, Boolean> mActiveUtterance = new HashMap<String, Boolean>();
    
    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextColor(Color.rgb(100, 0, 255));
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        mTts = new TextToSpeech(context, this);
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(final String utteranceId) {
                setmState(State.TALKING);
                //Add to active utterance list
                synchronized (MouthView.this){
                    mActiveUtterance.put(utteranceId, true);
                }
            }

            @Override
            public void onError(final String utteranceId) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDone(final String utteranceId) {
                synchronized (MouthView.this){
                    mActiveUtterance.remove(utteranceId);
                    if(mActiveUtterance.size() == 0){
                        setmState(State.NOT_TALKING);
                    }
                }
            }
        });

        mLastChange = SystemClock.uptimeMillis();
        
        mPath = new Path();
        mWavePath = new Path();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Style.STROKE);
        mMouthPaint = new Paint();
        mMouthPaint.setColor(Color.WHITE);

        
    }

    
    @Override
    protected void onDraw(final Canvas canvas) {
        
        drawMouth(getWidth(), getHeight());
        canvas.drawRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), 50, 50, mMouthPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(mWavePath, mPaint);
        
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
        final Visualizer mVisualizer = new Visualizer(0);
        mVisualizer.setDataCaptureListener(MouthView.this, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
    }


    /**
     * @param w
     * @param h
     */
    protected void drawMouth(final int w, final int h) {
        mWavePath.reset();
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
        
        int gap = 0;
        final int waves = 25;
        final int waveLength = mWave.length / waves;
        final int waveWidth = w / waves;
        int centerY = h / 2;
        mPath.moveTo(0, centerY);
        mWavePath.moveTo(0, centerY);
        int x = 0;
        int sum = 0;
        int count = 0;
        int section = 0;
        for(int i = 0; i < mWave.length; i ++){
            count++;
            final byte wave = mWave[i] <= -127 ||  mWave[i] >= 127 ? 0 : mWave[i];
            sum += wave;
            if(i % waveLength == 0){
                section++;
                if(section > waves / 2){
                    centerY -= h/2/waves;
                    gap += 5;
                } else {
                    centerY += h/2/waves;
                    gap -= 5;
                }
//                final int radius = h / 2;
//                final double angle = ((180 / waves) / (2 * Math.PI)) * section;
//                section++;
//                centerY = (int) ((h / 2) + radius * Math.cos(angle));
                final int waveHeight = (sum / count);
                final int destY = centerY + waveHeight;
                mPath.quadTo(x + waveWidth / 2, destY + gap, x + waveWidth, centerY + gap);
                mWavePath.quadTo(x + waveWidth / 2, centerY -waveHeight - gap, x + waveWidth, centerY - gap);
                //mPath.lineTo(x, destY);
                x += waveWidth;
                count = 0;
                sum = 0;
            }
            
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
     * @param state the mState to set
     */
    private void setmState(final State state) {
        this.mState = state;
        if(state == State.NOT_TALKING){
            //Let the listener know the the speech is complete
            final SpeechCompleteListener speechCompleteListener = mSpeechCompleteListener;
            if(speechCompleteListener != null){
                speechCompleteListener.onSpeechComplete();
                //Listeners only get informed once
                mSpeechCompleteListener = null;
            }
        }
        postInvalidate();
    }

    private enum State {
        TALKING, NOT_TALKING
    }

    @Override
    public void onWaveFormDataCapture(final Visualizer visualizer, final byte[] waveform, final int samplingRate) {
        //Log.d("wave", Arrays.toString(waveform));
        updateWave(waveform);
    }


    /**
     * @param waveform
     */
    public void updateWave(final byte[] waveform) {
        mWave = waveform;
        invalidate();
    }

    public void setOnSpeechCompleteListener(final SpeechCompleteListener speechCompleteListener){
        this.mSpeechCompleteListener = speechCompleteListener;
    }

    @Override
    public void onFftDataCapture(final Visualizer visualizer, final byte[] fft, final int samplingRate) {
        updateWave(fft);
    }
    
    public interface SpeechCompleteListener {
        public void onSpeechComplete();
    }
}
