package com.tesseractmobile.poketbot.robot;

public class RobotCommand {

    public enum RobotCommandType {
        NOD, SHAKE, LEFT, RIGHT
    }

    final private RobotCommandType robotCommandType;

    public RobotCommand(final RobotCommandType robotCommandType){
        this.robotCommandType = robotCommandType;
    }

    public RobotCommandType getCommandType() {
        return robotCommandType;
    }
}
