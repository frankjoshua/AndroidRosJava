package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.RobotEvent;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import io.fabric.sdk.android.Fabric;
import tag.TagGame;

public class BaseFaceActivity extends Activity implements OnClickListener, VoiceRecognitionListener, BodyConnectionListener, SpeechCompleteListener, SensorEventListener{

    private static final String TAG = BaseFaceActivity.class.getSimpleName();

    private static final int START_LISTENING = 1;
    private static final int START_LISTENING_AFTER_PROMPT = 2;
    public static final int TIME_BETWEEN_HUMAN_SPOTTING = 10000;

    private MouthView mouthView;
    private EyeView mLeftEye;
    private EyeView mRightEye;
    private ListView mTextListView;
    private SpeechAdapter mSpeechAdapter;
    private SensorData mSensorData = new SensorData();

    //Device sensor manager
    private SensorManager mSensorManager;


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_LISTENING) {
                mSpeechState = SpeechState.LISTENING;
                mVoiceRecognitionService.startListening();
            } else if (msg.what == START_LISTENING_AFTER_PROMPT) {
                startListening((String) msg.obj);
            }
        }

    };

    private Emotion mEmotion;

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
    private float[] mGravity;
    private float[] mGeomagnetic;
    private long mLastSensorTransmision;
    private int mSensorDelay = 500;
    private int mHumanCount = 0;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.robot_face);

        mouthView = (MouthView) findViewById(R.id.mouthView);
        mLeftEye = (EyeView) findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) findViewById(R.id.eyeViewRight);
        mTextListView = (ListView) findViewById(R.id.textList);

        //Setup list view for text
        mSpeechAdapter = new SpeechAdapter(this);
        mTextListView.setAdapter(mSpeechAdapter);

        mouthView.setOnSpeechCompleteListener(this);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);

        //Start senors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Allow user to control the volume
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
        if (bindService(bindIntent, voiceRecognitionServiceConnection, Service.BIND_AUTO_CREATE) == false) {
            throw new UnsupportedOperationException("Error binding to service");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //Unbind from voice recognition service
        final VoiceRecognitionService voiceRecognitionService = this.mVoiceRecognitionService;
        if (voiceRecognitionService != null) {
            voiceRecognitionService.unregisterVoiceRecognitionListener(this);
            unbindService(voiceRecognitionServiceConnection);
            voiceRecognitionServiceConnection = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Start listening for orientation
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);

        //Set initial state
        setEmotion(Emotion.JOY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for orientation
        mSensorManager.unregisterListener(this);
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

    protected void look(final float x, final float y, float z) {
        //Log.d(TAG, "x " + Float.toString(x) + " y " + Float.toString(y) + " z " + Float.toString(z));
        mLeftEye.look(x, y);
        mRightEye.look(x, y);
        if (SystemClock.uptimeMillis() - mLastHeadTurn > 350) {
            mLastHeadTurn = SystemClock.uptimeMillis();
            //Log.d(TAG, "x " + Float.toString(x) + " y " + Float.toString(y));
            if (x > 1.25f || x < .75f || y > 1.25f || y < .75f || z < .2f || z > .4f) {
                if (mBodyInterface.isConnected()) {
                    mSensorData.setFace_x(x);
                    mSensorData.setFace_y(y);
                    mSensorData.setFace_z(z);
                    sendSensorData();
                }
                if (z > .55f) {
                    setEmotion(Emotion.FEAR);
                }
            }
        }
    }

    protected void sendSensorData() {
        final long uptime = SystemClock.uptimeMillis();
        if(mBodyInterface.isConnected() && uptime > mLastSensorTransmision + mSensorDelay) {
            mLastSensorTransmision = uptime;
            sendData(mSensorData);
        }
    }

    /**
     * Sends data to the robot body
     *
     * @param data
     */
    final protected void sendData(final Object data) {
        mBodyInterface.sendObject(data);
    }

    /**
     * Sends JSON directly
     *
     * @param json
     */
    final protected void sendJson(final String json) {
        mBodyInterface.sendJson(json);
    }

    /**
     * Speak the text
     *
     * @return true is speech was sent to the mouth
     */
    final synchronized protected boolean say(final String text) {
        mLastHumanSpoted = SystemClock.uptimeMillis();

        if (mSpeechState != SpeechState.READY) {
            Log.d(TAG, "Could not speak \'" + text + "\', state is " + mSpeechState);
            return false;
        }
        mSpeechState = SpeechState.TALKING;

        //Unmute Audio
//        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);

        //Check if we are on the UI thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setText(text);
        } else {
            //If not post a runnable on th UI thread
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setText(text);
                }
            });
        }
        return true;
    }

    /**
     * Send the text ot the mouth view and adds text to the preview view
     *
     * @param text
     */
    private void setText(String text) {
        addTextToList(text, true);
        getMouthView().setText(text);
    }

    private void addTextToList(final String text, final boolean isPocketBot) {
        mSpeechAdapter.addText(text, isPocketBot);
    }

    /**
     * Speak the text then run the runnable
     *
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
     *
     * @param prompt null is OK
     */
    final synchronized protected void listen(final String prompt) {
        mLastHumanSpoted = SystemClock.uptimeMillis();
        if (Looper.myLooper() == Looper.getMainLooper()) {
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
     *
     * @param prompt
     */
    private void startListening(final String prompt) {
        //setEmotion(Emotion.SUPRISED);
        if (prompt != null) {
            getMouthView().setOnSpeechCompleteListener(this);
            if (say(prompt)) {
                mSpeechState = SpeechState.WAITING_TO_LISTEN;
            }
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
    final public void onTextInput(final String input) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addTextToList(input, false);
            }
        });

        doTextInput(input);
    }

    protected void doTextInput(String input) {
        new BotTask().execute(input);
    }

    @Override
    public void onVoiceRecognitionStateChange(VoiceRecognitionState state) {
        //Any state change is not listening
        if (state == VoiceRecognitionState.READY) {
            onSpeechComplete();
        }
    }

    @Override
    public void onVoiceRecognitionError(String text) {
        say(text);
    }

    @Override
    public boolean onProccessInput(final String input) {
        if (input.contains("game")) {
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

    final synchronized protected void humanSpotted(final int id) {

        if(id == SensorData.NO_FACE){
            mHumanCount--;
            if(mHumanCount == 0){
                mSensorData.setFace_id(id);
                onHumanLeft();
                sendSensorData();
            }
            return;
        }
        mHumanCount++;
        mSensorData.setFace_id(id);
        final long uptimeMillis = SystemClock.uptimeMillis();
        //Check if no human has been spotted for 10 seconds
        if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
            onHumanSpoted();
        }
        mLastHumanSpoted = uptimeMillis;
    }

    private void onHumanLeft() {
        say("Goodbye");
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
        if (mSpeechState == SpeechState.WAITING_TO_LISTEN) {
            mHandler.sendEmptyMessage(START_LISTENING);
        } else {
            mSpeechState = SpeechState.READY;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = lowPass(event.values.clone(), mGravity);
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                final int heading = (int) (Math.toDegrees(orientation[0]) + 360 + 180) % 360;
                if (Math.abs(heading - mSensorData.getHeading()) > 1) {
                    mSensorData.setHeading(heading);
                    sendSensorData();
                    //Log.d(TAG, " New Heading " + mHeading);
                }
            }
        }

    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.25f * (input[i] - output[i]);
        }
        return output;
    }

    protected SensorData getSensorData(){
        return mSensorData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                chatterBotThought.setEmotions(new String[]{"Happy"});
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
    }

    enum Emotion {
        JOY, ACCEPTED, AWARE, ANGER, SADNESS, REJECTED, SUPRISED, FEAR
    }


}
