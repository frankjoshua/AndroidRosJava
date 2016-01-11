package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.SensorData;

/**
 * Created by josh on 10/25/2015.
 */
abstract public class BaseFace implements RobotFace{
    protected RobotInterface mRobotInterface;

    final public void setRobotInterface(final RobotInterface robotInterface){
        this.mRobotInterface = robotInterface;
    }

    public void onControlReceived(final SensorData.Control message) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        sensorData.setControl(message);
        //Send data
        mRobotInterface.sendSensorData(false);
    }
}
