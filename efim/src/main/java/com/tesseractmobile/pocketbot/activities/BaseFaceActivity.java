package com.tesseractmobile.pocketbot.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
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
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

abstract public class BaseFaceActivity extends Activity implements OnClickListener, RecognitionListener {

    private static final String  SPEECH_INSTRUTIONS             = "";//"Touch my mouth if you want to say something";

    private static final int     VOICE_RECOGNITION_REQUEST_CODE = 0;

    private static final boolean HIDE_VOICE_PROMPT              = true;

    private MouthView            mouthView;
    private EyeView              mLeftEye;
    private EyeView              mRightEye;
    private SpeechRecognizer     mSpeechRecognizer;
    private final Handler        mHandler                       = new Handler();
    private boolean              mHideVoicePrompt;

    private Emotion              mEmotion;

    
    
    
    
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

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        // Load settings
        mHideVoicePrompt = HIDE_VOICE_PROMPT;

    }

    private boolean checkVoiceRecognition() {
        // Check if voice recognition is present
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            getMouthView().setEnabled(false);
            say("Voice recognizer not present");
            Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (SpeechRecognizer.isRecognitionAvailable(this) == false) {
            say("I have no voice recognization service available");
            return false;
        }

        return true;
    }
    
    

    @Override
    protected void onPause() {
        super.onPause();
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
    }

    
    
    

    @Override
    protected void onResume() {
        super.onResume();
        //Set initial state
        setEmotion(Emotion.ANGER);
        
        if (checkVoiceRecognition()) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
            mSpeechRecognizer.setRecognitionListener(this);
        }
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
        case R.id.eyeViewLeft:
            say("Ouch");
            fear();
            // finish();
            break;
        case R.id.eyeViewRight:
            //say("I'm going to kill you in my sleep... Oh wait, your sleep");
            say("Please don't poke my eye.");
            anger();
            break;
        case R.id.mouthView:
            listen(null);
            break;
        }
    }

    /**
     * Set look to fearful
     */
    private void fear() {
        mLeftEye.squintLeft();
        mRightEye.squintRight();
    }

    /**
     * Set look to angry
     */
    private void anger() {
        mLeftEye.squintRight();
        mRightEye.squintLeft();
    }

    /**
     * Set the face to mimic the emotional state
     * 
     * @param emotion
     */
    final public void setEmotion(final Emotion emotion) {
        if (mEmotion != emotion) {
            mEmotion = emotion;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
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
                        mLeftEye.wideOpenLeft();
                        mRightEye.wideOpenRight();
                        break;
                    case FEAR:
                        fear();
                        break;
                    case ANGER:
                        anger();
                        break;
                    default:
                        mLeftEye.squint();
                        mRightEye.squint();
                        say("I don't under stand the emotion " + emotion + ".");
                        break;
                    }
                }
            });
        }
    }

    protected void look(final float x, final float y){
        mLeftEye.look(x, y);
        mRightEye.look(x, y);
    }
    
    /**
     * Speak the text
     */
    final protected void say(final String text) {
        //Unmute Audio
//        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);

        //Check if we are on the UI thread
        if(Looper.myLooper() == Looper.getMainLooper()){
            getMouthView().setText(text);
        } else {
            //If not post a runnable on th UI thread
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    getMouthView().setText(text);
                }
            });
        }
    }
    
    /**
     * Speak the text then run the runnable
     * @param speechText
     * @param runnable
     */
    final protected void say(final String speechText, final Runnable runnable) {
        //Post the runnable when speech is complete
        getMouthView().setOnSpeechCompleteListener(new SpeechCompleteListener() {
            
            @Override
            public void onSpeechComplete() {
                runOnUiThread(runnable);
            }
        });
        //Speak the text
        say(speechText);
    }

    protected final MouthView getMouthView() {
        return mouthView;
    }

    /**
     * Speak the text then listen for a response<br>
     * Pass null to just start listening
     * @param prompt null is OK
     */
    final protected void listen(final String prompt) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            startListening(prompt);
        } else {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    startListening(prompt);
                }
            });
        }
    }

    /**
     * Must be run on the UI thread
     * @param prompt
     */
    private void startListening(final String prompt) {
        setEmotion(Emotion.SUPRISED);
        if(prompt != null){
            getMouthView().setOnSpeechCompleteListener(new SpeechCompleteListener(){

                @Override
                public void onSpeechComplete() {
                    mHandler.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            lauchListeningIntent(prompt);
                        }
                    }, 50);
                }
                
            });
            say(prompt);
        } else {
            lauchListeningIntent(prompt);
        }
        
    }

    /**
     * @param prompt
     */
    protected void lauchListeningIntent(final String prompt) {
        //Mute the audio to stop the beep
//        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);

        //Use Google Speech Recognizer
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        if(prompt != null){
             intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        }

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
        if (mHideVoicePrompt) {
            mSpeechRecognizer.startListening(intent);
        } else {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
        // say("I'm Listening");
        // Uncomment for test speech
        // new BotTask().execute("Are you listening?");
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
        // say("Sound levels changed to " + rmsdB + " decibals");
    }

    @Override
    public void onBufferReceived(final byte[] buffer) {
        say("I hear something");
    }

    @Override
    public void onEndOfSpeech() {
        //Show joy
        setEmotion(Emotion.JOY);
    }

    @Override
    public void onError(final int error) {
        setEmotion(Emotion.ANGER);
        switch (error) {
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            say("I didn't hear you. " + SPEECH_INSTRUTIONS);
            //listen(null);
            break;
        case SpeechRecognizer.ERROR_NO_MATCH:
            //say("I'm sorry, I could not understand you. " + SPEECH_INSTRUTIONS);
            listen(null);
            break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            say("I'm sorry, but my speech recognizer is busy. Who ever programmed me probably forgot to close the service properly.");
            break;
        default:
            say("I had and unknown error in my speech system. The error code is " + error + ". I'm sorry that I can not be more helpful.");
            break;
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                proccessSpeech(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
            } else {
                onError(resultCode);
            }
        } else {
            say("I had an unhandled error.");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResults(final Bundle results) {
        final ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        proccessSpeech(data);
    }

    /**
     * @param data
     */
    private void proccessSpeech(final ArrayList<String> data) {
        if (data != null && data.size() > 0) {
            final String responce = data.get(0);
            if (responce != null) {
                // say(responce);
                // Send text to the chat bot
                if(proccessInput(responce) == false){
                    onTextInput(responce);
                }
            } else {
                // Something went wrong
                say("Pardon? " + SPEECH_INSTRUTIONS);
            }
        } else {
            say("No data recieved.");
        }
    }

    /**
     * @param input
     */
    protected void onTextInput(final String input) {
        new BotTask().execute(input);
    }
    
    /**
     * Execute commands based on input
     * @param input
     * @return true if input handled
     */
    protected boolean proccessInput(final String input){
        if(input.contains("game")){
            say("My favorite game is solitaire", new Runnable() {
                
                @Override
                public void run() {
                    final Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.tesseractmobile.solitairemulti");
                    startActivity(launchIntent);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void onPartialResults(final Bundle partialResults) {
        say("I only heard a little of what you said");
    }

    @Override
    public void onEvent(final int eventType, final Bundle params) {
        say("Event " + eventType);
    }

    private class BotTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            // Create a chat bot
            final ChatterBotFactory factory = new ChatterBotFactory();
            ChatterBot bot1;
            try {
                // final ChatterBot bot2 =
                bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
                // bot1 = factory.create(ChatterBotType.CLEVERBOT);
                // bot1 = factory.create(ChatterBotType.JABBERWACKY);
            } catch (final Exception e) {
                say("There was an error loading ChatterBotFactory()");
                return null;
            }
            final ChatterBotSession bot1session = bot1.createSession();
            String response = null;
            try {
                // Get the response from the chat bot
                final ChatterBotThought chatterBotThought = new ChatterBotThought();
                chatterBotThought.setText(params[0]);
                chatterBotThought.setEmotions(new String[] { "Happy" });
                final ChatterBotThought responseThought = bot1session.think(chatterBotThought);
                // Check for emotions
                final String[] emotions = responseThought.getEmotions();
                if (emotions != null) {
                    response = emotions[0];
                }
                if (response == null) {
                    // No emotions use words
                    // Strip HTML
                    final String cleanedResponce = Html.fromHtml(responseThought.getText()).toString();
                    response = cleanedResponce;
                } else {
                    response = "I feel somthing. It might be " + response + ".";
                }
            } catch (final Exception e) {
                // Tell user what went wrong
                say("Error in BotTask.doInBackground");
                Log.e("BotTask", e.toString());
                return null;
            }
            if (response.length() != 0) {
                //Speak the text and listen for a response
                listen(response);
            } else {
                say("I can't think of anything to say.");
            }

            return null;
        }

        /**
         * Updates the text on the UI Thread
         * 
         * @param text
         */
        private void say(final String text) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    getMouthView().setText(text);
                }
            });
        }
    }

    enum Emotion {
        JOY, ACCEPTED, AWARE, ANGER, SADNESS, REJECTED, SUPRISED, FEAR
    }
    
    
}
