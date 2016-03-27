package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.AuthData;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.fragments.ApiAiKeyDialog;
import com.tesseractmobile.pocketbot.activities.fragments.CallbackFragment;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragment;
import com.tesseractmobile.pocketbot.activities.fragments.FaceTrackingFragment;
import com.tesseractmobile.pocketbot.activities.fragments.RobotSelectionDialog;
import com.tesseractmobile.pocketbot.activities.fragments.TextPreviewFragment;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragmentFactory;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.SpeechListener;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;

public class BaseFaceFragmentActivity extends FragmentActivity implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, SpeechListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, KeepAliveThread.KeepAliveListener, KeepAliveThread.InternetAliveListener {

    private static final String TAG = BaseFaceFragmentActivity.class.getSimpleName();



    public static final String FRAGMENT_FACE_TRACKING = "FACE_TRACKING";
    public static final String FRAGMENT_FACE = "FACE";
    public static final String FRAGMENT_PREVIEW = "PREVIEW";
    private static final int RC_GOOGLE_LOGIN = 1;
    private static final int RC_REQUEST_INVITE = 2;
    private static final int RC_SIGN_IN = 3;


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

    private Handler handler = new Handler();

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean mGoogleLoginClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
     * sign-in. */
    private ConnectionResult mGoogleConnectionResult;
    private SignInButton mSignInButton;
    private BroadcastReceiver mBatteryReceiver;
    private KeepAliveThread mKeepAliveThread;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        mRobotInterFace = Robot.get();

        setContentView(R.layout.main);

        if(Robot.get().isNew()){
            //After login show the robot selection dialog
            Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
                @Override
                public void onAuthComplete(final AuthData authData) {
                    showRobotSelectionDialog();
                }
            });
        }

        //Set on click listeners
        findViewById(R.id.btnApiAi).setOnClickListener(this);
        findViewById(R.id.btnFeedback).setOnClickListener(this);
        findViewById(R.id.btnSendInvite).setOnClickListener(this);
        findViewById(R.id.llTop).setOnClickListener(this);
        findViewById(R.id.tvModes).setOnClickListener(this);
        findViewById(R.id.tvSettings).setOnClickListener(this);
        findViewById(R.id.tvEmotions).setOnClickListener(this);

        //Hide views
        findViewById(R.id.llModes).setVisibility(View.GONE);
        findViewById(R.id.emotionsFragment).setVisibility(View.GONE);
        //findViewById(R.id.settingsFragment).setVisibility(View.GONE);

        //Setup face
        switchFace(PocketBotSettings.getSelectedFace(this));

        //Start senors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Allow user to control the volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        updateUI();

        //Show drawer to user
        peekDrawer((DrawerLayout) findViewById(R.id.drawer_layout));

        //Setup Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mSignInButton.setScopes(gso.getScopeArray());
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        if (PocketBotSettings.isAutoSignIn(this)) {
            //startSignin();
        }

    }

    @Override
    public void onHeartBeat() {
        //Keep Arduino awake
        mRobotInterFace.sendSensorData(false);
    }

    @Override
    public void onInternetTimeout() {
        mRobotInterFace.getSensorData().setControl(new SensorData.Control());
        mRobotInterFace.sendSensorData(true);
    }

    private void updateUI() {
        //Setup Robot Id
        final TextView tvRobotId = (TextView) findViewById(R.id.tvRobotId);
        tvRobotId.setText(PocketBotSettings.getRobotId(this));

        //Setup Robot Name
        final TextView tvRobotName = (TextView) findViewById(R.id.tvRobotName);
        tvRobotName.setText(PocketBotSettings.getRobotName(this));
    }

    private void onTokenReceived(final String token){

        Log.d(TAG, "Received token: " + token);
        mGoogleIntentInProgress = false;

        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
            Robot.get().setAuthToken(PocketBotSettings.getRobotId(this), token);
            Toast.makeText(this, "Google Sign-In Complete", Toast.LENGTH_LONG).show();
            mSignInButton.setEnabled(true);
            //Auto sign in next time
            PocketBotSettings.setAutoSignIn(this, true);
        }
    }

    protected void peekDrawer(final DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(Gravity.LEFT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        }, 1000);
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

    private void setupTextPreview(final TextPreviewFragment textPreviewFragment) {
        //Setup list view for text
        mSpeechAdapter = new SpeechAdapter(BaseFaceFragmentActivity.this);
        ListView listView = textPreviewFragment.getListView();
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
        //Listen for speech to update preview
        mRobotInterFace.registerSpeechListener(this);
        //Keep alive thread
        if(PocketBotSettings.isKeepAlive(this)) {
            mKeepAliveThread = new KeepAliveThread(this, this);
            mKeepAliveThread.startThread();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Listen for preference changes
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(this, this);
        //Stop listening for speech to update preview
        mRobotInterFace.unregisterSpeechListener(this);
        //Keep alive thread
        if(PocketBotSettings.isKeepAlive(this)) {
            mKeepAliveThread.stopThread();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Start listening for orientation
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        //Listen to proximity sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);
        //Listen for battery status
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBatteryReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                //information about battery status
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level / (float)scale;
                SensorData sensorData = mRobotInterFace.getSensorData();
                sensorData.getSensor().battery = (int) (batteryPct * 100);
            }
        };
        registerReceiver(mBatteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for orientation
        mSensorManager.unregisterListener(this);
        //Stop listening for battery status
        unregisterReceiver(mBatteryReceiver);
    }

    /**
     * Set the face to mimic the emotional state
     *
     * @param emotion
     */
    final public void setEmotion(final Emotion emotion) {
        mRobotInterFace.setEmotion(emotion);
    }


    @Override
    public void onSpeechIn(String speech) {
        addTextToList(speech, false);
    }

    @Override
    public void onSpeechOut(String speech) {
        addTextToList(speech, true);
    }


    private void addTextToList(final String text, final boolean isPocketBot) {

        final SpeechAdapter speechAdapter = this.mSpeechAdapter;
        if(speechAdapter != null){
            speechAdapter.addText(text, isPocketBot);
        }
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
            //mGravity = lowPass(event.values.clone(), mGravity);
            mGravity = lowPass(event.values, mGravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
            mGeomagnetic = lowPass(event.values, mGeomagnetic);
        }
        SensorData sensorData = mRobotInterFace.getSensorData();
        if (mGravity != null && mGeomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(ROTATION, INCLINATION, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(ROTATION, ORIENTATION);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                final int heading = (int) (Math.toDegrees(ORIENTATION[0]) + 360 + 180) % 360;
                if (Math.abs(heading - sensorData.getSensor().heading) > 1) {
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
        final FaceFragment faceFragment = FaceFragmentFactory.getFaceFragment(faceId);
        final boolean isUseFaceTracking = faceFragment.isUseFaceTracking() && checkGooglePlayServices();


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
                final TextPreviewFragment textPreviewFragment = new TextPreviewFragment();
                final FaceTrackingFragment faceTrackingFragment = new FaceTrackingFragment();
                //Create FaceTrackingFragment
                ft.add(R.id.overlayView, faceTrackingFragment, FRAGMENT_FACE_TRACKING);
                //Create Preview Fragment
                ft.add(R.id.topOverlayView, textPreviewFragment, FRAGMENT_PREVIEW);
                //Set up a listener for when the view is created
                if (textPreviewFragment != null) {
                    textPreviewFragment.setOnCompleteListener(new CallbackFragment.OnCompleteListener() {

                        @Override
                        public void onComplete() {
                            setupTextPreview(textPreviewFragment);
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
        if(PocketBotSettings.KEY_SELECTED_FACE.equals(key)){
            final int faceId = sharedPreferences.getInt(key, PocketBotSettings.DEFAULT_FACE_ID);
            switchFace(faceId);
        } else if(PocketBotSettings.KEY_KEEP_ALIVE.equals(key)) {
            final boolean keepAlive = sharedPreferences.getBoolean(key, PocketBotSettings.DEFAULT_KEEP_ALIVE);
            if(keepAlive){
                //Start the thread
                mKeepAliveThread = new KeepAliveThread(this, this);
                mKeepAliveThread.startThread();
            } else {
                //Stop the thread
                mKeepAliveThread.stopThread();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
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
            case R.id.llTop:
                showRobotSelectionDialog();
                break;
            case R.id.btnSendInvite:
                Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
                    @Override
                    public void onAuthComplete(final AuthData authData) {
                        final String displayName = (String) authData.getProviderData().get("displayName");
                        final String robotName = PocketBotSettings.getRobotName(BaseFaceFragmentActivity.this);
                        final String message = displayName + " is giving you control of " + robotName;
                        Intent intent = new AppInviteInvitation.IntentBuilder("PocketBot Control Invite")
                                .setMessage(message)
                                .setDeepLink(Uri.parse("http://pocketbot.tesseractmobile.com/usbserialfragment/" + PocketBotSettings.getRobotId(BaseFaceFragmentActivity.this)))
                                //.setCustomImage(Uri.parse((String) authData.getProviderData().get("profileImageURL")))
                                //.setCallToActionText("invitation_cta")
                                .setEmailHtmlContent("<html><body>"
                                        + "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">" + "Click here to start controlling " + robotName + ".<br><br>"
                                        + "<img src=\"" + (String) authData.getProviderData().get("profileImageURL") + "\" height=\"130\" width=\"130\"/></a>"
                                        + "</body></html>")
                                .setEmailSubject("PocketBot Control Invite from " + displayName)
                                .build();
                        startActivityForResult(intent, RC_REQUEST_INVITE);
                    }
                });
                break;
            case R.id.sign_in_button:
                startSignin();
                break;
            case R.id.btnApiAi:
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                ApiAiKeyDialog apiAiKeyDialog = new ApiAiKeyDialog();
                apiAiKeyDialog.show(fragmentTransaction2, "API_AI_FRAGMENT");
                break;
            case R.id.btnFeedback:
                //Launch user feedback website
                String url = "https://feedback.userreport.com/eb1b841d-4f55-44a7-9432-36e77efefb77/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.tvModes:
                //Toggle Modes View visibility
                final View modesLayout = findViewById(R.id.llModes);
                toggleViewVisibility(modesLayout);
                break;
            case R.id.tvSettings:
                toggleSettingsFragment();
                break;
            case R.id.tvEmotions:
                final View emotionsFragment = findViewById(R.id.emotionsFragment);
                toggleViewVisibility(emotionsFragment);
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }
    }

    private void toggleSettingsFragment() {
        final View settingsFragment = findViewById(R.id.llSettings);
        toggleViewVisibility(settingsFragment);
    }

    private void showRobotSelectionDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                RobotSelectionDialog robotSelectionDialog = new RobotSelectionDialog();
                robotSelectionDialog.setOnlyUserRobots(true);
                robotSelectionDialog.setSignInOnClickListener(BaseFaceFragmentActivity.this);
                robotSelectionDialog.show(getSupportFragmentManager(), "ROBOT_SELECTION_DIALOG");
                robotSelectionDialog.setOnRobotSelectedListener(new RobotSelectionDialog.OnRobotSelectedListener() {
                    @Override
                    public void onRobotSelected(RobotInfo.Settings model) {
                        PocketBotSettings.setRobotId(BaseFaceFragmentActivity.this, model.prefs.robot_id);
                        //Check if name should be set
                        if (model.prefs.robot_name.equals(PocketBotSettings.DEFAULT_ROBOT_NAME)) {
                            //Launch name change dialog
                            //Open draw
                            //Tell user to change the robot name
                            Toast.makeText(BaseFaceFragmentActivity.this, "You should change your robot name.", Toast.LENGTH_LONG).show();
                            toggleSettingsFragment();
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
                        }
                    }
                });
            }
        });
    }

    private void toggleViewVisibility(final View view) {
        if(view.getVisibility() == View.GONE){
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void startSignin() {
        //mSignInButton.setEnabled(false);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
//        mGoogleLoginClicked = true;
//        if (!mGoogleApiClient.isConnecting()) {
//            if (mGoogleConnectionResult != null) {
//                resolveSignInError();
//            } else if (mGoogleApiClient.isConnected()) {
//                getGoogleOAuthTokenAndLogin();
//            } else {
//                /* connect API now */
//                Log.d(TAG, "Trying to connect to Google API");
//                mGoogleApiClient.connect();
//            }
//        }
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        Log.e(TAG, "Resolving sign in Error");
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
                Log.d(TAG, "Launching google login");
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "IntentSender.SendIntentException " + e.toString());
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "No sign error resolution");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case RC_GOOGLE_LOGIN:
                    /* This was a request by the Google API */
                    if (resultCode != RESULT_OK) {
                        mGoogleLoginClicked = false;
                    }
                    mGoogleIntentInProgress = false;
                    if (!mGoogleApiClient.isConnecting()) {
                        mGoogleApiClient.connect();
                        Log.d(TAG, "Connecting Google Sign In");
                    }
                    break;
                case RC_REQUEST_INVITE:
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                    Log.d(TAG, "Sent " + ids.length + " Invitations");
                    break;
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    Log.d(TAG, "Trying to connect to Google API");
                    token = GoogleAuthUtil.getToken(BaseFaceFragmentActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                    Log.d(TAG, "Token read: " + token);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    onTokenReceived(token);
                } else if (errorMessage != null) {
                    Toast.makeText(BaseFaceFragmentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        };
        task.execute();
    }


    protected RobotInterface getRobotInterface() {
        return mRobotInterFace;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, result.toString());
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
        getGoogleOAuthTokenAndLogin();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
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
