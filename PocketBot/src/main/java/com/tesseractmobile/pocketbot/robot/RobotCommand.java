package com.tesseractmobile.pocketbot.robot;

public class RobotCommand {

    public enum RobotCommandType {
        NOD, SHAKE, LEFT, RIGHT, STOP, FORWARD, BACKWARD
    }

    final private RobotCommandType robotCommandType;

    public int target;
    public int command;
    public int value;
    public int time;

    public RobotCommand(final RobotCommandType robotCommandType){
        this.robotCommandType = robotCommandType;
    }

    public RobotCommandType getCommandType() {
        return robotCommandType;
    }
}
