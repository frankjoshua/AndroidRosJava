package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by josh on 8/19/2015.
 */
public class AiActivity extends GoogleFaceDetectActivity {

    private final String CLIENT_ACCESS_TOKEN = "443dddf4747d4408b0e9451d4d53f201";
    private final String SUBSCRIPTION_KEY = "1eca9ad4-74e8-4d3a-afea-7131df82d19b";

    private AIDataService mAiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AIConfiguration aiConfig = new AIConfiguration(CLIENT_ACCESS_TOKEN, SUBSCRIPTION_KEY, AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
        mAiDataService = new AIDataService(this, aiConfig);
    }

//    @Override
//    protected void lauchListeningIntent(String prompt) {
//        mAiService.startListening();
//    }

    @Override
    public void onTextInput(final String input) {
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
        if(action.equals("move")){
            final String direction = result.getStringParameter("direction");
            final String measurement = result.getStringParameter("measurement");
            final int distance = result.getIntParameter("distance");
            move(direction, measurement, distance);
        }
        final String speech = result.getFulfillment().getSpeech();
        if(speech.equals("")){
            super.onTextInput(result.getResolvedQuery());
        } else {
            listen(speech);
        }
    }

    protected void move(String direction, String measurement, int distance) {
        say("I have no body. I can't move");
    }

}
