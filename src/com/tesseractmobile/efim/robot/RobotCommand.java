package com.tesseractmobile.efim.robot;

public class RobotCommand {

    public enum RobotCommandType {
        NOD, SHAKE
    }

    final private RobotCommandType robotCommandType;

    public RobotCommand(final RobotCommandType robotCommandType){
        this.robotCommandType = robotCommandType;
    }

    public RobotCommandType getCommandType() {
        return robotCommandType;
    }
}
