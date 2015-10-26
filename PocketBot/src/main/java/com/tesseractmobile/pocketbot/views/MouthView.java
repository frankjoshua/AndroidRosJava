package com.tesseractmobile.pocketbot.views;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Build;
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
    private final Paint mMouthPaint;
    private SpeechCompleteListener mSpeechCompleteListener;

    static final private RectF DEST_RECT = new RectF();
    private HashMap<String, Boolean> mActiveUtterance = new HashMap<String, Boolean>();

    private Bitmap[] mMouthBitmaps;
    private int mCurrentBitmap = 0;
    private long mLastChange;

    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextColor(Color.rgb(100, 0, 255));
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        mTts = new TextToSpeech(context, this);
        if(Build.VERSION.SDK_INT >= 15) {
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onStart(final String utteranceId) {
                    setmState(State.TALKING);
                    //Add to active utterance list
                    synchronized (MouthView.this) {
                        mActiveUtterance.put(utteranceId, true);
                    }
                }

                @Override
                public void onError(final String utteranceId) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onDone(final String utteranceId) {
                    synchronized (MouthView.this) {
                        mActiveUtterance.remove(utteranceId);
                        if (mActiveUtterance.size() == 0) {
                            setmState(State.NOT_TALKING);
                        }
                    }
                }
            });

        }

        mMouthPaint = new Paint();
        mMouthPaint.setColor(Color.WHITE);

        mMouthBitmaps = new Bitmap[4];
        mMouthBitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.staticmouthhappy);
        mMouthBitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth1);
        mMouthBitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth2);
        mMouthBitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth3);
    }

    
    @Override
    protected void onDraw(final Canvas canvas) {

        if(mState == State.TALKING){
            invalidate();
            if(SystemClock.uptimeMillis() - mLastChange > 75){
                mLastChange = SystemClock.uptimeMillis();
                mCurrentBitmap++;
                if(mCurrentBitmap > 3){
                    mCurrentBitmap = 1;
                }
            }
        }
        DEST_RECT.set(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRoundRect(DEST_RECT, 50, 50, mMouthPaint);
        canvas.drawBitmap(mMouthBitmaps[mCurrentBitmap], null, DEST_RECT, null);
        //Draw text
        //super.onDraw(canvas);
    }


    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final Visualizer mVisualizer = new Visualizer(0);
        mVisualizer.setDataCaptureListener(MouthView.this, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
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
            mCurrentBitmap = 0;
            //Let the listener know the the speech is complete
            final SpeechCompleteListener speechCompleteListener = mSpeechCompleteListener;
            if(speechCompleteListener != null){
                speechCompleteListener.onSpeechComplete();
                //Listeners only get informed once
                //mSpeechCompleteListener = null;
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
