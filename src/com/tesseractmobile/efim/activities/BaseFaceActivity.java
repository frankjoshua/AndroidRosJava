package com.tesseractmobile.efim.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.efim.R;
import com.tesseractmobile.efim.views.EyeView;
import com.tesseractmobile.efim.views.MouthView;

abstract public class BaseFaceActivity extends Activity implements OnClickListener, RecognitionListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 0;
    private MouthView        mouthView;
    private EyeView          mLeftEye;
    private EyeView          mRightEye;
    private SpeechRecognizer mSpeechRecognizer;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.robot_face);

        mouthView = (MouthView) findViewById(R.id.mouthView);
        mLeftEye = (EyeView) findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) findViewById(R.id.eyeViewRight);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        checkVoiceRecognition();
        
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);
    }

    private void checkVoiceRecognition() {
        // Check if voice recognition is present
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            getMouthView().setEnabled(false);
            getMouthView().setText("Voice recognizer not present");
            Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            say("Ouch");
            finish();
            break;
        case R.id.eyeViewRight:
            say("I'm going to kill you in my sleep... Oh wait, your sleep");
            break;
        case R.id.mouthView:
            listen();
            // final SpeechRecognizer speechRecognizer =
            // SpeechRecognizer.createSpeechRecognizer(this);
            //
            // speechRecognizer.startListening(new Intent());
            // new BotTask().execute();
            break;
        }
    }

    /**
     * Set the face to mimic the emotional state
     * @param emotion
     */
    public void setEmotion(final Emotion emotion){
        switch (emotion) {
        case ACCEPTED:
            mLeftEye.squint();
            mRightEye.squint();
            break;
        case SUPRISED:
            mLeftEye.open();
            mRightEye.open();
            mLeftEye.blink();
            mRightEye.blink();
            break;
        case AWARE:
            mLeftEye.open();
            mRightEye.squint();
            break;
        case JOY:
            mLeftEye.open();
            mRightEye.open();
            break;
        default:
            mLeftEye.squint();
            mRightEye.squint();
            say("I don't under stand the emotion " + emotion + ".");
            break;
        }
    }
    
    /**
     * Speak the text
     */
    protected void say(final String text) {
        getMouthView().setText(text);
    }

    protected final MouthView getMouthView() {
        return mouthView;
    }

    public void listen() {
        setEmotion(Emotion.SUPRISED);
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help you?");

        // Given an hint to the recognizer about what the user is going to say
        // There are two form of language model available
        // 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        // 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
        // and its domain.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be
        // sorted where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        // Start the Voice recognizer activity for the result.
        //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        mSpeechRecognizer.startListening(intent);
        //Uncomment for test speech
        //new BotTask().execute("Are you listening?");
    }

    @Override
    public void onReadyForSpeech(final Bundle params) {
        setEmotion(Emotion.ACCEPTED);
    }

    @Override
    public void onBeginningOfSpeech() {
       setEmotion(Emotion.AWARE);
    }

    @Override
    public void onRmsChanged(final float rmsdB) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBufferReceived(final byte[] buffer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEndOfSpeech() {
        setEmotion(Emotion.JOY);
    }

    @Override
    public void onError(final int error) {
        getMouthView().setText("Error, Pardon?");
    }

    @Override
    public void onResults(final Bundle results) {
        final ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(data != null && data.size() > 0){
            final String responce = data.get(0);
            if(responce != null){
                getMouthView().setText(responce);
                new BotTask().execute(responce);
            } else {
                //Something went wrong
                getMouthView().setText("Pardon?");
            }
        }
    }

    @Override
    public void onPartialResults(final Bundle partialResults) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEvent(final int eventType, final Bundle params) {
        // TODO Auto-generated method stub
        
    }
    
    private class BotTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

                //Create a chat bot
                final ChatterBotFactory factory = new ChatterBotFactory();
                ChatterBot bot1;
                try {
                    // final ChatterBot bot2 =
                    bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
                    //bot1 = factory.create(ChatterBotType.CLEVERBOT);
                    //bot1 = factory.create(ChatterBotType.JABBERWACKY);
                } catch (final Exception e) {
                    say("There was an error loading ChatterBotFactory()");
                    return null;
                }
                final ChatterBotSession bot1session = bot1.createSession();
                String response = null;
                try {
                    //Get the response from the chat bot
                    final ChatterBotThought chatterBotThought = new ChatterBotThought();
                    chatterBotThought.setText(params[0]);
                    chatterBotThought.setEmotions(new String[]{"Happy"});
                    final ChatterBotThought responseThought = bot1session.think(chatterBotThought);
                    //Check for emotions
                    final String[] emotions = responseThought.getEmotions();
                    if(emotions != null){
                        response = emotions[0];
                    }
                    if(response == null){
                        //No emotions use words
                        response = responseThought.getText();
                    } else {
                        response = "I feel somthing. It migh be " + response + ".";
                    }
                } catch (final Exception e){
                    //Tell user what went wrong
                    say("Error in BotTask.doInBackground");
                    Log.e("BotTask", e.toString());
                    return null;
                }
                if(response.length() != 0){
                    say(response);
                    //TODO: This should use a callback from the MouthView when the text is stopped
                    //Delay for every character
                    final int delayMillis = response.length() * 88 + 1000;
                    mHandler.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            listen();
                        }
                    }, delayMillis);
                    
                } else {
                   say("I can't think of anything to say.");
                }

            return null;
        }

        /**
         * Updates the text on the UI Thread
         * @param text
         */
        private void say(final String text){
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    getMouthView().setText(text);
                }
            });
        }
    }
    
    enum Emotion {
        JOY, ACCEPTED, AWARE, ANGRY, SADNESS, REJECTED, SUPRISED
    }
}
