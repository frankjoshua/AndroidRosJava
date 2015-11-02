package com.tesseractmobile.pocketbot.activities;

import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.fragments.CallbackFragment;
import com.tesseractmobile.pocketbot.activities.fragments.ControlFaceFragment;
import com.tesseractmobile.pocketbot.activities.fragments.EfimFaceFragment;
import com.tesseractmobile.pocketbot.activities.fragments.FaceFragment;
import com.tesseractmobile.pocketbot.activities.fragments.FaceTrackingFragment;
import com.tesseractmobile.pocketbot.activities.fragments.PreviewFragment;
import com.tesseractmobile.pocketbot.activities.fragments.SignInFragment;
import com.tesseractmobile.pocketbot.activities.fragments.TelepresenceFaceFragment;
import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.PocketBotProtocol;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.RobotEvent;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class BaseFaceActivity extends FragmentActivity implements  VoiceRecognitionListener, BodyConnectionListener,  SensorEventListener, SpeechCompleteListener, RobotInterface, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final String TAG = BaseFaceActivity.class.getSimpleName();

    private static final int START_LISTENING = 1;
    private static final int START_LISTENING_AFTER_PROMPT = 2;
    public static final int TIME_BETWEEN_HUMAN_SPOTTING = 10000;
    public static final String FRAGMENT_FACE_TRACKING = "FACE_TRACKING";
    public static final String FRAGMENT_FACE = "FACE";
    public static final String FRAGMENT_PREVIEW = "PREVIEW";


    private RobotFace mRobotFace;
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


    private long mLastHumanSpoted;
    private ServiceConnection voiceRecognitionServiceConnection;

    private VoiceRecognitionService mVoiceRecognitionService;

    //Storage for sensors
    static private float ROTATION[] = new float[9];
    static private float INCLINATION[] = new float[9];
    static private float ORIENTATION[] = new float[3];

    protected BodyInterface mBodyInterface = new BodyInterface() {
        @Override
        public void sendObject(Object object) {
            sendJson(null);
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void sendJson(String json) {
            //Do nothing
            //say("I can't feel my wheels!");
        }

        @Override
        public void sendBytes(byte[] bytes) {

        }
    };
    private SpeechState mSpeechState = SpeechState.READY;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private long mLastSensorTransmision;
    private int mSensorDelay = 0;
    private int mHumanCount = 0;
    private boolean mFaceTrackingActive;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.main);
        //Set on click listeners
        findViewById(R.id.btnTelepresence).setOnClickListener(this);
        findViewById(R.id.btnControl).setOnClickListener(this);
        findViewById(R.id.btnSignIn).setOnClickListener(this);
        findViewById(R.id.btnFace).setOnClickListener(this);

        //Setup face
        switchFace(PocketBotSettings.getSelectedFace(this));

        //Start senors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Allow user to control the volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        peekDrawer((DrawerLayout) findViewById(R.id.drawer_layout));
    }

    protected void peekDrawer(final DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(Gravity.LEFT);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        }, 3000);
//        final long downTime = SystemClock.uptimeMillis();
//        final long eventTime = SystemClock.uptimeMillis() + 100;
//        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0, 100, 0);
//        drawerLayout.dispatchTouchEvent(motionEvent);
//        motionEvent.recycle();
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final long downTime = SystemClock.uptimeMillis();
//                final long eventTime = SystemClock.uptimeMillis() + 100;
//                MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 0, 100, 0);
//                drawerLayout.dispatchTouchEvent(motionEvent);
//                motionEvent.recycle();
//            }
//        }, (long) (2 * DateUtils.SECOND_IN_MILLIS));
    }

    private void setupTextPreview(final PreviewFragment previewFragment) {
        //Setup list view for text
        mSpeechAdapter = new SpeechAdapter(BaseFaceActivity.this);
        ListView listView = previewFragment.getListView();
        listView.setAdapter(mSpeechAdapter);
    }

    /**
     * Will direct user to play store to update Google Play Services if needed
     * @return true if google play services is available
     */
    private boolean checkGooglePlayServices() {
        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, GooglePlayServicesUtil.getErrorString(status));

            // ask user to update google play services.
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
            return false;
        } else {
            Log.i(TAG, GooglePlayServicesUtil.getErrorString(status));
            // google play services is updated.
            //your code goes here...
            return true;
        }
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

        //Listen for preference changes
        PocketBotSettings.registerOnSharedPreferenceChangeListener(this, this);
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

        //Listen for preference changes
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Start listening for orientation
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        //Listen to proximity sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for orientation
        mSensorManager.unregisterListener(this);
    }

    /**
     * Set the face to mimic the emotional state
     *
     * @param emotion
     */
    final public void setEmotion(final Emotion emotion) {
        mRobotFace.setEmotion(emotion);
    }

    @Override
    public void look(final float x, final float y, float z) {
        mRobotFace.look(x, y, z);
        mSensorData.setFace_x(x);
        mSensorData.setFace_y(y);
        mSensorData.setFace_z(z);
        sendSensorData(false);
    }

    /**
     * Sends sensor data if enough time has passed
     * May drop data
     * @param force true if data must be sent
     */
    @Override
    public void sendSensorData(final boolean force) {
        final long uptime = SystemClock.uptimeMillis();
        if(force || uptime >= mLastSensorTransmision + mSensorDelay) {
            mLastSensorTransmision = uptime;
            if(mBodyInterface.isConnected()){
                final PocketBotProtocol.PocketBotMessage data = SensorData.toPocketBotMessage(mSensorData);
                //Send raw data
                mBodyInterface.sendBytes(data.toByteArray());
            }
        }
    }

    /**
     * Speak the text
     *
     * @return true is speech was sent to the mouth
     */
    @Override
    final synchronized public boolean say(final String text) {
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
        mRobotFace.setOnSpeechCompleteListener(this);
        mRobotFace.say(text);
    }

    private void addTextToList(final String text, final boolean isPocketBot) {
        mSpeechAdapter.addText(text, isPocketBot);
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
            mRobotFace.setOnSpeechCompleteListener(this);
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
            say("My favorite game is solitaire");
            final Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.tesseractmobile.solitairemulti");
            startActivity(launchIntent);
            return true;
        }
        return false;
    }

    @Override
    final synchronized public void humanSpotted(final int id) {
        final long uptimeMillis = SystemClock.uptimeMillis();
        if(id == SensorData.NO_FACE){
            mHumanCount--;
            if(mHumanCount == 0){
                mSensorData.setFace_id(id);
                if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
                    onHumanLeft();
                }
                sendSensorData(true);
            }
            return;
        }
        mHumanCount++;
        mSensorData.setFace_id(id);
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
            boolean success = SensorManager.getRotationMatrix(ROTATION, INCLINATION, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(ROTATION, ORIENTATION);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                final int heading = (int) (Math.toDegrees(ORIENTATION[0]) + 360 + 180) % 360;
                if (Math.abs(heading - mSensorData.getHeading()) > 1) {
                    mSensorData.setHeading(heading);
                    sendSensorData(false);
                    //Log.d(TAG, " New Heading " + heading);
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            final float distance = event.values[0];
            //Distance is either touching or not
            mSensorData.setProximity(distance < 1.0f);
            sendSensorData(true);
            //Log.d(TAG, "Proximity " + Float.toString(distance));
        }
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.25f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public SensorData getSensorData(){
        return mSensorData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void listen() {
        listen(null);
    }

    private void switchFace(int faceId){
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = supportFragmentManager.beginTransaction();
        final FaceFragment faceFragment;
        final Boolean isUseFaceTracking;
        switch (faceId){
            case 0:
                faceFragment = new EfimFaceFragment();
                isUseFaceTracking = true && checkGooglePlayServices();
                break;
            case 1:
                faceFragment = new ControlFaceFragment();
                isUseFaceTracking = false;
                break;
            case 2:
                faceFragment = new TelepresenceFaceFragment();
                isUseFaceTracking = false;
                break;
            default:
                throw new UnsupportedOperationException("Unknown face id " + faceId);
        }

        if(supportFragmentManager.findFragmentByTag(FRAGMENT_FACE) != null){
            ft.replace(R.id.faceView, faceFragment, FRAGMENT_FACE);
        } else {
            ft.add(R.id.faceView, faceFragment, FRAGMENT_FACE);
        }

        faceFragment.setOnCompleteListener(new CallbackFragment.OnCompleteListener() {
            @Override
            public void onComplete() {
                mRobotFace = faceFragment.getRobotFace(BaseFaceActivity.this);
            }
        });
        if(isUseFaceTracking){
            if(mFaceTrackingActive == false) {
                mFaceTrackingActive = true;
                final PreviewFragment previewFragment = new PreviewFragment();
                final FaceTrackingFragment faceTrackingFragment = new FaceTrackingFragment();
                //Create FaceTrackingFragment
                ft.add(R.id.overlayView, faceTrackingFragment, FRAGMENT_FACE_TRACKING);
                //Create Preview Fragment
                ft.add(R.id.overlayView, previewFragment, FRAGMENT_PREVIEW);
                //Set up a listener for when the view is created
                if (previewFragment != null) {
                    previewFragment.setOnCompleteListener(new CallbackFragment.OnCompleteListener() {

                        @Override
                        public void onComplete() {
                            setupTextPreview(previewFragment);
                        }
                    });
                }
                if (faceTrackingFragment != null) {
                    faceTrackingFragment.setRobotInterface(this);
                }
            }
        } else {
            if(mFaceTrackingActive) {
                mFaceTrackingActive = false;
                final Fragment faceTrackingFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_FACE_TRACKING);
                if (faceFragment != null) {
                    ft.remove(faceTrackingFragment);
                }
                final Fragment previewFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_PREVIEW);
                if (previewFragment != null) {
                    ft.remove(previewFragment);
                }
            }
        }
        //Commit changes
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PocketBotSettings.SELECTED_FACE.equals(key)){
            final int faceId = sharedPreferences.getInt(key, PocketBotSettings.DEFAULT_FACE_ID);
            switchFace(faceId);
        }
    }

    /**
     * Time between data sent to the robot
     * @param i in millis
     */
    protected void setSensorDelay(int i) {
        mSensorDelay = i;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id){
            case R.id.btnSignIn:
                //Launch sign in fragment
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.main_window, new SignInFragment(), "SIGN_IN_FRAGMENT");
                fragmentTransaction.commit();
                break;
            case R.id.btnTelepresence:
                PocketBotSettings.setSelectedFace(BaseFaceActivity.this, 2);
                break;
            case R.id.btnControl:
                PocketBotSettings.setSelectedFace(BaseFaceActivity.this, 1);
                break;
            case R.id.btnFace:
                PocketBotSettings.setSelectedFace(BaseFaceActivity.this, 0);
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
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

    public enum Emotion {
        JOY, ACCEPTED, AWARE, ANGER, SADNESS, REJECTED, SUPRISED, FEAR
    }


}
