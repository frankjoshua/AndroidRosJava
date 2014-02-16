package com.tesseractmobile.efim.robot;

public class CommandContract {
    //Targets
    public final static byte TAR_SERVO_HEAD_HORZ = (byte) 0xa; // 10
    public final static byte TAR_SERVO_HEAD_VERT = (byte) 0xb; // 11
    //Commands
    public static final byte CMD_MOVE    = (byte) 0x2;
}
