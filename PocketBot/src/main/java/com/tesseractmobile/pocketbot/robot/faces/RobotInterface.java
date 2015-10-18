package com.tesseractmobile.pocketbot.robot.faces;

/**
 * Created by josh on 10/17/2015.
 */
public interface RobotInterface {
    void listen();

    void humanSpotted(int id);

    void look(float x, float y, float z);
}
