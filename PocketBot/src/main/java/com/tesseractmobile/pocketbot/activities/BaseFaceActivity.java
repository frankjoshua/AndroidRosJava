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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.PocketBotProtocol;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.RobotEvent;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

import io.fabric.sdk.android.Fabric;

public class BaseFaceActivity extends FragmentActivity implements  SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final String TAG = BaseFaceActivity.class.getSimpleName();



    public static final String FRAGMENT_FACE_TRACKING = "FACE_TRACKING";
    public static final String FRAGMENT_FACE = "FACE";
    public static final String FRAGMENT_PREVIEW = "PREVIEW";


    //private RobotFace mRobotFace;
    private SpeechAdapter mSpeechAdapter;


    //Device sensor manager
    private SensorManager mSensorManager;

    //Storage for sensors
    static private float ROTATION[] = new float[9];
    static private float INCLINATION[] = new float[9];
    static private float ORIENTATION[] = new float[3];

    private float[] mGravity;
    private float[] mGeomagnetic;

    private boolean mFaceTrackingActive;

    private RobotInterface mRobotInterFace;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        mRobotInterFace = Robot.get();

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
        new Handler().postDelayed(new Runnable() {
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

        //Listen for preference changes
        PocketBotSettings.registerOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();

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
        mRobotInterFace.setEmotion(emotion);
    }

    /**
     * Send the text ot the mouth view and adds text to the preview view
     *
     * @param text
     */
    private void setText(String text) {
        addTextToList(text, true);
        //mRobotFace.setOnSpeechCompleteListener(this);
        mRobotInterFace.say(text);
    }

    private void addTextToList(final String text, final boolean isPocketBot) {

        final SpeechAdapter speechAdapter = this.mSpeechAdapter;
        if(speechAdapter != null){
            speechAdapter.addText(text, isPocketBot);
        }
    }


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



//    @Override
//    public boolean onProccessInput(final String input) {
//        if (input.contains("game")) {
//            say("My favorite game is solitaire");
//            final Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.tesseractmobile.solitairemulti");
//            startActivity(launchIntent);
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = lowPass(event.values.clone(), mGravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
        }
        SensorData sensorData = mRobotInterFace.getSensorData();
        if (mGravity != null && mGeomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(ROTATION, INCLINATION, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(ROTATION, ORIENTATION);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                final int heading = (int) (Math.toDegrees(ORIENTATION[0]) + 360 + 180) % 360;
                if (Math.abs(heading - sensorData.getHeading()) > 1) {
                    sensorData.setHeading(heading);
                    mRobotInterFace.sendSensorData(false);
                    //Log.d(TAG, " New Heading " + heading);
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            final float distance = event.values[0];
            //Distance is either touching or not
            sensorData.setProximity(distance < 1.0f);
            mRobotInterFace.sendSensorData(true);
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                mRobotInterFace.setRobotFace(faceFragment.getRobotFace(mRobotInterFace));
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
                    faceTrackingFragment.setRobotInterface(mRobotInterFace);
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
        mRobotInterFace.setSensorDelay(i);
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

    protected RobotInterface getRobotInterface() {
        return mRobotInterFace;
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
                mRobotInterFace.say("There was an error loading ChatterBotFactory()");
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
                mRobotInterFace.say("Error in BotTask.doInBackground");
                Log.e("BotTask", e.toString());
                return null;
            }
            if (response.length() != 0) {
                //Speak the text and listen for a response
                mRobotInterFace.listen(response);
            } else {
                mRobotInterFace.say("I can't think of anything to say.");
            }

            return null;
        }
    }


}
