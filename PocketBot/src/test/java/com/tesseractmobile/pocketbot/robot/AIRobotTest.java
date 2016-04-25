package com.tesseractmobile.pocketbot.robot;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by josh on 1/14/2016.
 */
public class AIRobotTest {

    private AIRobot mAiRobot;

    @Before
    public void setUp() throws Exception {
        mAiRobot = new AIRobot();
    }

    @Test
    public void testSetAI() throws Exception {
        final String[] testText = {""};
        mAiRobot.setAI(new AI() {
            @Override
            public void input(String text, AIListener aiListener) {
                testText[0] = text;
            }
        });

        final String TEST_TEXT = "Hello";
        mAiRobot.onTextInput(TEST_TEXT);

        assertEquals(TEST_TEXT, testText[0]);
    }

    @Test
    public void testOnProccessInput() throws Exception {

    }

    @Test
    public void testOnTextInput() throws Exception {

    }

    @Test
    public void testRegisterSpeechListener() throws Exception {

    }

    @Test
    public void testUnregisterSpeechListener() throws Exception {

    }
}