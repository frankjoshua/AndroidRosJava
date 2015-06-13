package com.tesseractmobile.efim.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tesseractmobile.efim.R;

public class EyeView extends View {
    
    private final Handler mHandler = new Handler();
    
    public EyeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            setBackgroundResource(R.drawable.eye_closing1);
            break;
        case MotionEvent.ACTION_MOVE:
            setBackgroundResource(R.drawable.eye_closing2);
            break;
        case MotionEvent.ACTION_UP:
            blink();
            break;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Blink the eye
     * Must be called from the UI Thread
     */
    public void blink(){
        //Poor mans animation
        final int baseSpeed = 100;
        postDelayed(R.drawable.eye_closing1, 0);
        final int closeDelay = baseSpeed;
        postDelayed(R.drawable.eye_closing2, closeDelay);
        final int secondOpenDelay = closeDelay + baseSpeed * 4;
        postDelayed(R.drawable.eye_closing1, secondOpenDelay);
        final int endDelay = secondOpenDelay + baseSpeed;
        postDelayed(R.drawable.eye_open, endDelay);
    }
    
    /**
     * Open the eye
     */
    public void open() {
        postDelayed(R.drawable.eye_open, 0);
    }
    
    /**
     * Squint the eye
     */
    public void squint() {
        postDelayed(R.drawable.eye_closing1, 0);
    }

    /**
     * @param eyePosition
     */
    protected void postDelayed(final int eyePosition, final int delay) {
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                setBackgroundResource(eyePosition);
            }
        }, delay);
    }

}
