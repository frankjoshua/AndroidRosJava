package com.tesseractmobile.efim.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.efim.R;
import com.tesseractmobile.efim.views.MouthView;

abstract public class BaseFaceActivity extends Activity implements OnClickListener {
    
    private MouthView               mouthView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.robot_face);

        mouthView = (MouthView) findViewById(R.id.mouthView);
        
        //Setup click listeners
        findViewById(R.id.eyeViewLeft).setOnClickListener(this);
        findViewById(R.id.eyeViewRight).setOnClickListener(this);
        findViewById(R.id.mouthView).setOnClickListener(this);
        
        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            getMouthView().setText("Ouch");
            finish();
            break;
        case R.id.eyeViewRight:
            getMouthView().setText("I'm going to kill you in my sleep... Oh wait, your sleep");
            break;
        case R.id.mouthView:
            new BotTask().execute();
            break;  
        }
    }
    
    protected final MouthView getMouthView(){
        return mouthView;
    }
    
    private class BotTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                final ChatterBotFactory factory = new ChatterBotFactory();

                final ChatterBot bot1 = factory.create(ChatterBotType.JABBERWACKY);
                final ChatterBotSession bot1session = bot1.createSession();

                //final ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
                final ChatterBot bot2 = factory.create(ChatterBotType.JABBERWACKY);
                final ChatterBotSession bot2session = bot2.createSession();

                String s = "Hello";
                int count = 2;
                while (count > 0) {
                    count--;
                    
                    if(s.equals("")){
                        s = "Are you still there?";
                    }
                    final String s2 = s;
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            getMouthView().setText(s2);
                        }
                    });
                   
                    
                    s = bot1session.think(s);
                    if(s.equals("")){
                        s = "Can you hear me?";
                    }
                    //s = Integer.toString(count);
                    final String s3 = s;
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            getMouthView().setText(s3);
                        }
                    });
                    s = bot2session.think(s);
                    
                }
                
            } catch (final Exception e) {
                // TODO: handle exception
            }
            return null;
        }
        
    }
}
