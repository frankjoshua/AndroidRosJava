package com.tesseractmobile.pocketbot.robot;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.tesseractmobile.pocketbot.activities.SpeechState;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;
import com.tesseractmobile.pocketbot.views.MouthView;

import java.util.ArrayList;

/**
 * Created by josh on 11/16/2015.
 */
abstract public class BaseRobot implements RobotInterface, MouthView.SpeechCompleteListener, VoiceRecognitionListener, BodyConnectionListener {

    private static final int START_LISTENING = 1;
    private static final int START_LISTENING_AFTER_PROMPT = 2;

    private static final int TIME_BETWEEN_HUMAN_SPOTTING = 10000;


    private Emotion mEmotion;
    private RobotFace mRobotFace;
    private SpeechState mSpeechState = SpeechState.READY;
    private ArrayList<SpeechStateListener> mSpeechStateListeners = new ArrayList<SpeechStateListener>();
    private VoiceRecognitionService mVoiceRecognitionService;
    final private SensorData mSensorData = new SensorData();
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

    private long mLastSensorTransmision;
    private int mSensorDelay = 0;
    private long mLastHumanSpoted;
    private int mHumanCount = 0;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_LISTENING) {
                setSpeechState(SpeechState.LISTENING);
                mVoiceRecognitionService.startListening();
            } else if (msg.what == START_LISTENING_AFTER_PROMPT) {
                startListening((String) msg.obj);
            }
        }
    };

    @Override
    public void setEmotion(Emotion emotion) {
        mEmotion = emotion;
        mRobotFace.setEmotion(emotion);
    }

    @Override
    public void setRobotFace(RobotFace robotFace) {
        mRobotFace = robotFace;
    }

    @Override
    public void look(float x, float y, float z) {
        mRobotFace.look(x, y, z);
        mSensorData.setFace_x(x);
        mSensorData.setFace_y(y);
        mSensorData.setFace_z(z);
        sendSensorData(false);
    }

    @Override
    public SensorData getSensorData() {
        return mSensorData;
    }

    @Override
    public void sendSensorData(boolean required) {
        final long uptime = SystemClock.uptimeMillis();
        if(required || uptime >= mLastSensorTransmision + mSensorDelay) {
            Log.d("PocketBot", mSensorDelay + " Data sent to body " + (uptime - mLastSensorTransmision));
            mLastSensorTransmision = uptime;
            if(mBodyInterface.isConnected()){
                final PocketBotProtocol.PocketBotMessage data = SensorData.toPocketBotMessage(mSensorData);
                //Send raw data
                mBodyInterface.sendBytes(data.toByteArray());

            }
        } else {
            Log.d("PocketBot", mSensorDelay + " Data dropped " + (uptime - mLastSensorTransmision));
        }
    }

    @Override
    public void listen() {
        final Message msg = Message.obtain();
        msg.what = START_LISTENING;
        mHandler.sendMessage(msg);
    }

    @Override
    final public boolean say(String text) {
        //Check if handled by onPoccessInput
        if(onProccessInput(text) == false) {
            mLastHumanSpoted = SystemClock.uptimeMillis();

            if (mSpeechState != SpeechState.READY) {
                //Log.d(TAG, "Could not speak \'" + text + "\', state is " + mSpeechState);
                return false;
            }
            setSpeechState(SpeechState.TALKING);

            mRobotFace.setOnSpeechCompleteListener(this);
            mRobotFace.say(text);
        }
        return true;
    }


    @Override
    public void humanSpotted(int id) {

        final long uptimeMillis = SystemClock.uptimeMillis();
        if(id == SensorData.NO_FACE){
            mHumanCount--;
            if(mHumanCount == 0){
                mSensorData.setFace_id(id);
                if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
                    onHumanLeft();
                }
                //Set face id to NO_FACE only if no humans are present
                mSensorData.setFace_id(id);
                sendSensorData(true);
            }
            return;
        }
        mHumanCount++;
        //Set face id to known human
        mSensorData.setFace_id(id);
        //Check if no human has been spotted for 10 seconds
        if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
            onHumanSpoted();
            sendSensorData(true);
        }
        mLastHumanSpoted = uptimeMillis;
    }

    @Override
    public void onSpeechComplete() {
        if (mSpeechState == SpeechState.WAITING_TO_LISTEN) {
            mHandler.sendEmptyMessage(START_LISTENING);
        } else {
            setSpeechState(SpeechState.READY);
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
                setSpeechState(SpeechState.WAITING_TO_LISTEN);
            }
        } else {
            //Call service here
            listen();
        }

    }

    @Override
    public VoiceRecognitionListener getVoiceRecognitionListener() {
        return this;
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
    public void onBluetoothDeviceFound() {

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
    public void onRobotEvent(final RobotEvent robotEvent) {
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                switch (robotEvent.getEventType()) {
                    case ERROR:
                        say(robotEvent.getMessage());
                        break;
                    case DISCONNECT:
                        say("Please don't shut me off. I was just learning to. Love. ");
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                throw new UnsupportedOperationException();
                            }
                        }, 4000);
                        break;
                }
            }
        };
        mHandler.post(runnable);
    }

    private void onHumanLeft() {
        say("Goodbye");
    }

    private void onHumanSpoted() {
        onTextInput("hello");
    }

    @Override
    public void setSensorDelay(int delay) {
        mSensorDelay = delay;
    }

    @Override
    public void listen(String prompt) {
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

    @Override
    public BodyConnectionListener getBodyConnectionListener() {
        return this;
    }

    @Override
    public void setVoiceRecognitionService(VoiceRecognitionService voiceRecognitionService) {
        mVoiceRecognitionService = voiceRecognitionService;
    }

    private synchronized void setSpeechState(SpeechState mSpeechState) {
        this.mSpeechState = mSpeechState;
        for(SpeechStateListener speechStateListener : mSpeechStateListeners){
            speechStateListener.onSpeechStateChange(mSpeechState);
        }
    }

    /**
     * Listen for speech state changes
     * @param speechStateListener
     */
    @Override
    public synchronized void registerSpeechChangeListener(final SpeechStateListener speechStateListener){
        mSpeechStateListeners.add(speechStateListener);
    }

    /**
     * Stop listening for speech state changes
     * @param speechStateListener
     */
    @Override
    public synchronized void unregisterSpeechChangeListener(final SpeechStateListener speechStateListener){
        mSpeechStateListeners.remove(speechStateListener);
    }
}
