package com.tesseractmobile.pocketbot.robot;

public class RobotCommand {

    public enum RobotCommandType {
        NOD, SHAKE, LEFT, RIGHT, STOP, FORWARD, BACKWARD
    }

    final private RobotCommandType robotCommandType;

    public RobotCommand(final RobotCommandType robotCommandType){
        this.robotCommandType = robotCommandType;
    }

    public RobotCommandType getCommandType() {
        return robotCommandType;
    }
}
