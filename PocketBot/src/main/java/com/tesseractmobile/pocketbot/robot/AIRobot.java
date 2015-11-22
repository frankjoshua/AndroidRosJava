package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 11/16/2015.
 */
public class AIRobot extends BaseRobot {
    private AI mAI;

    public void setAI(final AI ai){
        mAI = ai;
    }

    @Override
    public boolean onProccessInput(String text) {
        say(text);
        return true;
    }

    @Override
    public void onTextInput(String text) {
        mAI.input(text, null);
    }
}
