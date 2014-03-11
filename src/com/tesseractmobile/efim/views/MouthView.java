package com.tesseractmobile.efim.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.tesseractmobile.efim.R;

public class MouthView extends TextView implements OnInitListener {

    final Handler handler = new Handler();
    private final TextToSpeech mTts;
    private boolean isTalkReady;
    
    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextSize(40);
        setTextColor(Color.rgb(100, 0, 255));
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mTts = new TextToSpeech(context, this);
    }

    @Override
    public void setText(final CharSequence text, final BufferType type) {
        if (handler != null) {
            setBackgroundResource(R.drawable.mouth_speaking_1);
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    setBackgroundResource(R.drawable.mouth_static);
                }
            }, 500);
            if(isTalkReady){
                mTts.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        super.setText(text, type);
    }

    @Override
    public void onInit(final int status) {
        isTalkReady = true;
    }

    
}
