package com.tesseractmobile.efim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tesseractmobile.efim.R;

public class EyeView extends View {

    public EyeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        //setBackgroundColor(Color.CYAN);
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
            setBackgroundResource(R.drawable.eye_open);
            break;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

}
