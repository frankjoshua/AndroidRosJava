package com.tesseractmobile.pocketbot.robot;

import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;

/**
 * Created by josh on 11/16/2015.
 */
public class Robot extends AIRobot {

    static private RobotInterface mRobot;

    private Robot(){};

    static public void init(){
        if(mRobot == null){
            mRobot = new Robot();
        }
    }

    static public RobotInterface get(){
        return mRobot;
    }

}
