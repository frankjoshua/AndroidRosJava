package com.tesseractmobile.pocketbot.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.tesseractmobile.pocketbot.robot.CommandContract;
import com.tesseractmobile.pocketbot.robot.RobotCommand;
import com.tesseractmobile.pocketbot.robot.RobotEvent;

import io.fabric.sdk.android.Fabric;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by josh on 8/19/2015.
 */
public class AiActivity extends GoogleFaceDetectActivity {

    public static final String PARAM_NUMBER = "number";
    private final String CLIENT_ACCESS_TOKEN = "443dddf4747d4408b0e9451d4d53f201";
    private final String SUBSCRIPTION_KEY = "1eca9ad4-74e8-4d3a-afea-7131df82d19b";
    private final Handler handler = new Handler();
    private AIDataService mAiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        final AIConfiguration aiConfig = new AIConfiguration(CLIENT_ACCESS_TOKEN, SUBSCRIPTION_KEY, AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
        mAiDataService = new AIDataService(this, aiConfig);
    }

//    @Override
//    protected void lauchListeningIntent(String prompt) {
//        mAiService.startListening();
//    }


    @Override
    protected void onHumanSpoted() {
        onTextInput("hello");
    }

    @Override
    protected void doTextInput(final String input) {
        if(input == null || input.equals("")){
            return;
        }
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(input);

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final AIResponse aiResponse = mAiDataService.request(aiRequest);
                    // process response object here...
                    handleAiResponse(aiResponse);


                } catch (final AIServiceException e) {
                    say("I had an unhandled error.");
                }
                return null;
            }
        }.execute();

    }

    private void handleAiResponse(final AIResponse aiResponse){
        final Result result = aiResponse.getResult();
        final String action = result.getAction();
        if(action.equals(CommandContract.ACTION_ARDUINO)){
            sendJson(aiResponse.toString());
        } else if(action.equals(CommandContract.ACTION_MOVE)){
            final String direction = result.getStringParameter(CommandContract.PARAM_DIRECTION);
            final String measurement = result.getStringParameter(CommandContract.PARAM_MEASUREMENT);
            final int distance = result.getIntParameter(CommandContract.PARAM_DISTANCE);
            move(direction, measurement, distance);
        } else if(action.equals(CommandContract.ACTION_FLASH)){
            final int times = result.getIntParameter(PARAM_NUMBER);
            flash(times);
        } else if(action.equals(CommandContract.ACTION_EMOTION)){
            emotion(result);
        } else if (action.equals(CommandContract.ACTION_SETTINGS)){
            final String previewSetting = result.getStringParameter(CommandContract.PARAM_PREVIEW, "false");
            final boolean shouldPreview = previewSetting.equalsIgnoreCase("true");
            PocketBotSettings.setShowPreview(this, shouldPreview);
        }
        final String speech = result.getFulfillment().getSpeech();
        if(speech.equals("")){
            super.doTextInput(result.getResolvedQuery());
        } else {
            listen(speech);
        }
    }

    private void emotion(Result result) {
        final String emotion = result.getStringParameter(CommandContract.PARAM_EMOTION);

        if(emotion.equals(CommandContract.EMOTION_ANGER)){
            setEmotion(Emotion.ANGER);
        } else if(emotion.equals(CommandContract.EMOTION_JOY)){
            setEmotion(Emotion.JOY);
        } else if(emotion.equals(CommandContract.EMOTION_ACCEPTED)){
            setEmotion(Emotion.ACCEPTED);
        } else if(emotion.equals(CommandContract.EMOTION_AWARE)){
            setEmotion(Emotion.AWARE);
        } else if(emotion.equals(CommandContract.EMOTION_SURPRISED)){
            setEmotion(Emotion.SUPRISED);
        } else if(emotion.equals(CommandContract.EMOTION_FEAR)){
            setEmotion(Emotion.FEAR);
        } else {
            say("I had a new emotion... I don't understand, " + emotion);
        }
    }

    private void flash(int times) {
        final RobotCommand command = new RobotCommand(RobotCommand.RobotCommandType.STOP);
        command.target = 1;
        command.command = 2;
        command.value = times;

        sendData(command);
    }

    protected void move(String direction, String measurement, int distance) {
        say("I have no body. I can't move");
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
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                finish();
                            }
                        }, 4000);
                        break;
                }
            }
        };
        handler.post(runnable);

    }
}
