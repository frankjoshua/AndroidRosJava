package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.SensorData;

/**
 * Created by josh on 10/17/2015.
 */
public interface RobotInterface {
    /**
     * Start voice recognition
     */
    void listen();

    /**
     *
     * @param id of the face SensorData.NO_FACE if face was lost
     */
    void humanSpotted(int id);

    /**
     * 0.0 to 2.0,  1.0 is center
     * @param x
     * @param y
     * @param z distance in percent 1.0 close 0.0 far
     */
    void look(float x, float y, float z);

    /**
     * @return reference to the sensor data
     */
    SensorData getSensorData();
}
