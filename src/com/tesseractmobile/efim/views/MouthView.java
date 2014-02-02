package com.tesseractmobile.efim.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.tesseractmobile.efim.R;

public class MouthView extends TextView {

    final Handler handler = new Handler();
    
    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextSize(40);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
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
        }
        super.setText(text, type);
    }

}
