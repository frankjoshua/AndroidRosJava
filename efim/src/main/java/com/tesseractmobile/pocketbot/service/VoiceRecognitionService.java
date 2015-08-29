package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 8/25/2015.
 */
public class VoiceRecognitionService extends Service implements RecognitionListener{

    private static final String  SPEECH_INSTRUTIONS             = "";//"Touch my mouth if you want to error something";

    private static final int     VOICE_RECOGNITION_REQUEST_CODE = 0;
    private static final boolean HIDE_VOICE_PROMPT              = true;

    final private IBinder binder = new LocalBinder();
    private SpeechRecognizer mSpeechRecognizer;
    private boolean              mHideVoicePrompt;
    private boolean mIsListening;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceRecognitionState mState;


    @Override
    public void onCreate() {
        super.onCreate();
        // Load settings
        mHideVoicePrompt = HIDE_VOICE_PROMPT;
        if (checkVoiceRecognition()) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
            mSpeechRecognizer.setRecognitionListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
    }

    private void error(final String text){
        mVoiceRecognitionListener.onVoiceRecognitionError(text);
    }

    private void listen(final String text){
        if(text == null){
            lauchListeningIntent(text);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void proccessInput(final String text){
        if(mVoiceRecognitionListener.onProccessInput(text) == false){
            onTextInput(text);
        }
    }

    private void onTextInput(final String text){
        mVoiceRecognitionListener.onTextInput(text);
    }

    private void setState(final VoiceRecognitionState state){
        mState = state;
        mVoiceRecognitionListener.onVoiceRecognitionStateChange(state);
    }

    private boolean checkVoiceRecognition() {
        // Check if voice recognition is present
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            //getMouthView().setEnabled(false);
            error("Voice recognizer not present");
            Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (SpeechRecognizer.isRecognitionAvailable(this) == false) {
            error("I have no voice recognization service available");
            return false;
        }

        return true;
    }

    /**
     * @param prompt
     */
    protected synchronized void lauchListeningIntent(final String prompt) {
        if(mIsListening){
            return;
        }
        mIsListening = true;
        //Mute the audio to stop the beep
//        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);

        //Use Google Speech Recognizer
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should error.
        if(prompt != null){
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        }

        // Given an hint to the recognizer about what the user is going to error
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
            //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            throw new UnsupportedOperationException();
        }
        // error("I'm Listening");
        // Uncomment for test speech
        // new BotTask().execute("Are you listening?");
    }

    @Override
    public void onReadyForSpeech(final Bundle params) {
        setState(VoiceRecognitionState.READY);
        //setEmotion(Emotion.ACCEPTED);
    }

    @Override
    public void onBeginningOfSpeech() {
        setState(VoiceRecognitionState.BEGINNING_OF_SPEECH);
        //setEmotion(Emotion.AWARE);
    }

    @Override
    public void onRmsChanged(final float rmsdB) {
        // error("Sound levels changed to " + rmsdB + " decibals");
    }

    @Override
    public void onBufferReceived(final byte[] buffer) {
        error("I hear something");
    }

    @Override
    public void onEndOfSpeech() {
        setState(VoiceRecognitionState.END_OF_SPEECH);
        //Show joy
        //setEmotion(Emotion.JOY);
    }

    @Override
    public void onError(final int error) {
        mIsListening = false;
        setState(VoiceRecognitionState.ERROR);
        //setEmotion(Emotion.ANGER);
        switch (error) {
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                error("I didn't hear you. " + SPEECH_INSTRUTIONS);
                //listen(null);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                //error("I'm sorry, I could not understand you. " + SPEECH_INSTRUTIONS);
                listen(null);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                error("I'm sorry, but my speech recognizer is busy. Who ever programmed me probably forgot to close the service properly.");
                break;
            default:
                error("I had and unknown error in my speech system. The error code is " + error + ". I'm sorry that I can not be more helpful.");
                break;
        }

    }

    @Override
    public synchronized void onResults(final Bundle results) {
        mIsListening = false;
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
                proccessInput(responce);
            } else {
                // Something went wrong
                error("Pardon? " + SPEECH_INSTRUTIONS);
            }
        } else {
            error("No data recieved.");
        }
    }

    @Override
    public void onPartialResults(final Bundle partialResults) {
        error("I only heard a little of what you said");
    }

    @Override
    public void onEvent(final int eventType, final Bundle params) {
        error("Event " + eventType);
    }

    public void registerVoiceRecognitionListener(final VoiceRecognitionListener voiceRecognitionListener){
        this.mVoiceRecognitionListener = voiceRecognitionListener;
    }

    public void unregisterVoiceRecognitionListener(final VoiceRecognitionListener voiceRecognitionListener){
        this.mVoiceRecognitionListener = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startListening() {
        lauchListeningIntent(null);
    }

    public class LocalBinder extends Binder {
        public VoiceRecognitionService getService(){
            return VoiceRecognitionService.this;
        }
    }
}
