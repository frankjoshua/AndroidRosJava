package com.tesseractmobile.efim.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.tesseractmobile.efim.R;
import com.tesseractmobile.efim.views.MouthView;

abstract public class BaseFaceActivity extends Activity implements OnClickListener {
    
    private MouthView               mouthView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.robot_face);

        mouthView = (MouthView) findViewById(R.id.mouthView);
        
        findViewById(R.id.eyeViewLeft).setOnClickListener(this);
        findViewById(R.id.eyeViewRight).setOnClickListener(this);
        findViewById(R.id.mouthView).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            getMouthView().setText(R.string.better_not_die);
            break;
        case R.id.eyeViewRight:
            getMouthView().setText(R.string.ghost_in_the_machine);
            break;
        case R.id.mouthView:
            finish();
            break;
        }
    }
    
    protected final MouthView getMouthView(){
        return mouthView;
    }
}
