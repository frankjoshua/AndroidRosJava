package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.gson.Gson;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.CommandContract;
import com.tesseractmobile.pocketbot.robot.RobotCommand;
import com.tesseractmobile.pocketbot.robot.RobotEvent;
import com.tesseractmobile.pocketbot.service.BluetoothService;
import com.tesseractmobile.pocketbot.service.UsbConnectionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import java.nio.charset.Charset;

import io.fabric.sdk.android.Fabric;

public class BaseFaceActivity extends Activity implements OnClickListener, VoiceRecognitionListener, BodyConnectionListener, SpeechCompleteListener {

    private static final String TAG = BaseFaceActivity.class.getSimpleName();

    private static final int START_LISTENING = 1;
    private static final int START_LISTENING_AFTER_PROMPT = 2;
    public static final int TIME_BETWEEN_HUMAN_SPOTTING = 10000;

    private MouthView            mouthView;
    private EyeView              mLeftEye;
    private EyeView              mRightEye;

    private final Handler        mHandler                       = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == START_LISTENING){
                mSpeechState = SpeechState.LISTENING;
                mVoiceRecognitionService.startListening();
            } else if (msg.what == START_LISTENING_AFTER_PROMPT){
                startListening((String) msg.obj);
            }
        }

    };

    private Emotion              mEmotion;

    private long mLastHumanSpoted;
    private ServiceConnection voiceRecognitionServiceConnection;

    private VoiceRecognitionService mVoiceRecognitionService;

    protected BodyInterface mBodyInterface = new BodyInterface() {
        @Override
        public void sendObject(Object object) {
            sendJson(null);
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void sendJson(String json) {
            //Do nothing
            say("I can't feel my wheels!");
        }
    };
    private SpeechState mSpeechState = SpeechState.READY;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.robot_face);

        mouthView = (MouthView) findViewById(R.id.mouthView);
        mLeftEye = (EyeView) findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) findViewById(R.id.eyeViewRight);

        mouthView.setOnSpeechCompleteListener(this);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to voice recognition service
        voiceRecognitionServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mVoiceRecognitionService = ((VoiceRecognitionService.LocalBinder) service).getService();
                mVoiceRecognitionService.registerVoiceRecognitionListener(BaseFaceActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        final Intent bindIntent = new Intent(this, VoiceRecognitionService.class);
        if(bindService(bindIntent, voiceRecognitionServiceConnection, Service.BIND_AUTO_CREATE) == false){
            throw new UnsupportedOperationException("Error binding to service");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //Unbind from voice recognition service
        final VoiceRecognitionService voiceRecognitionService = this.mVoiceRecognitionService;
        if(voiceRecognitionService != null) {
            voiceRecognitionService.unregisterVoiceRecognitionListener(this);
            unbindService(voiceRecognitionServiceConnection);
            voiceRecognitionServiceConnection = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Set initial state
        setEmotion(Emotion.JOY);
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
                        //say("I don't under stand the emotion " + emotion + ".");
                        break;
                    }
                }
            });
        }
    }

    private long mLastHeadTurn = SystemClock.uptimeMillis();
    protected void look(final float x, final float y){
        if(SystemClock.uptimeMillis() - mLastHeadTurn > 25) {
            mLastHeadTurn = SystemClock.uptimeMillis();
            mLeftEye.look(x, y);
            mRightEye.look(x, y);
            Log.d(TAG, "x " + Float.toString(x) + " y " + Float.toString(y));
            if (x > 1.5f || x < .5f) {
                if (mBodyInterface.isConnected()) {
                    final RobotCommand command = new RobotCommand();
                    command.target = CommandContract.TAR_SERVO_PAN;
                    if (x > 1.5f) {
                        command.command = CommandContract.CMD_LEFT;
                    } else {
                        command.command = CommandContract.CMD_RIGHT;
                    }
                    command.value = 1;
                    sendData(command);
                }
            }
        }
    }

    /**
     * Sends data to the robot body
     * @param data
     */
    final protected void sendData(final Object data){
        mBodyInterface.sendObject(data);
    }

    /**
     * Sends JSON directly
     * @param json
     */
    final protected void sendJson(final String json){
        mBodyInterface.sendJson(json);
    }

    /**
     * Speak the text
     */
    final protected void say(final String text) {
        mLastHumanSpoted = SystemClock.uptimeMillis();

        if(mSpeechState != SpeechState.READY){
            Log.d(TAG, "Could not speak \'" + text +  "\', state is " + mSpeechState);
            return;
        }

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
        mLastHumanSpoted = SystemClock.uptimeMillis();
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
        mLastHumanSpoted = SystemClock.uptimeMillis();
        if(Looper.myLooper() == Looper.getMainLooper()){
            startListening(prompt);
        } else {
            final Message msg = Message.obtain();
            msg.obj = prompt;
            msg.what = START_LISTENING_AFTER_PROMPT;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * Must be run on the UI thread
     * @param prompt
     */
    private void startListening(final String prompt) {
        //setEmotion(Emotion.SUPRISED);
        if(prompt != null){
            getMouthView().setOnSpeechCompleteListener(this);
            say(prompt);
            mSpeechState = SpeechState.WAITING_TO_LISTEN;
        } else {
            //Call service here
            mSpeechState = SpeechState.LISTENING;
            mVoiceRecognitionService.startListening();
        }

    }


//    @Override
//    protected synchronized void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
//        mIsListening = false;
//        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                proccessSpeech(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
//            } else {
//                onError(resultCode);
//            }
//        } else {
//            say("I had an unhandled error.");
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }



    /**
     * @param input
     */
    public void onTextInput(final String input) {
        new BotTask().execute(input);
    }

    @Override
    public void onVoiceRecognitionStateChange(VoiceRecognitionState state) {
        //Any state change is not listening
        onSpeechComplete();
    }

    @Override
    public void onVoiceRecognitionError(String text) {
        say(text);
    }

    @Override
    public boolean onProccessInput(final String input){
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

    final protected void humanSpotted(){
        final long uptimeMillis = SystemClock.uptimeMillis();
        //Check if no human has been spotted for 10 seconds
        if(uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING){
            onHumanSpoted();
        }
        mLastHumanSpoted = uptimeMillis;
    }

    protected void onHumanSpoted() {
        listen("Hello human.");
    }

    @Override
    public void onBluetoothDeviceFound() {
        say("Bluetooth device found");
    }

    @Override
    public void onError(int i, String error) {
        say(error);
    }

    @Override
    public void onBodyConnected(BodyInterface bodyInterface) {
        this.mBodyInterface = bodyInterface;
        say("Body interface established");
    }

    @Override
    public void onRobotEvent(RobotEvent robotEvent) {

    }

    @Override
    public void onSpeechComplete() {
        if(mSpeechState == SpeechState.LISTENING){
            mSpeechState = SpeechState.READY;
        } else if(mSpeechState == SpeechState.WAITING_TO_LISTEN){
            mHandler.sendEmptyMessage(START_LISTENING);
        }
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
