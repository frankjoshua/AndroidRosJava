package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.activities.BaseFaceActivity;
import com.tesseractmobile.pocketbot.views.MouthView;

/**
 * Created by josh on 10/17/2015.
 */
public interface RobotFace {
    /**
     * Display this emotion
     * @param emotion
     */
    public void setEmotion(BaseFaceActivity.Emotion emotion);

    /**
     * Direction to look in
     * @param x
     * @param y
     * @param z
     */
    public void look(float x, float y, float z);

    /**
     * Speak the text
     * @param text
     */
    public void say(String text);

    void setOnSpeechCompleteListener(MouthView.SpeechCompleteListener speechCompleteListener);
}
