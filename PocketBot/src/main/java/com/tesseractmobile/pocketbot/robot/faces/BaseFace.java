package com.tesseractmobile.pocketbot.robot.faces;

/**
 * Created by josh on 10/25/2015.
 */
abstract public class BaseFace implements RobotFace{
    protected RobotInterface mRobotInterface;

    final public void setRobotInterface(final RobotInterface robotInterface){
        this.mRobotInterface = robotInterface;
    }
}
