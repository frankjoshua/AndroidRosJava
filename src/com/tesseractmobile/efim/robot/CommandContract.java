package com.tesseractmobile.efim.robot;

/**
 * Hold static variables that match the TargetRegistration.h
 * 
 * @author josh
 * 
 */
public class CommandContract {
    // Targets
    public final static byte TAR_SERVO_HEAD_HORZ = (byte) 0xa;    // 10
    public final static byte TAR_SERVO_HEAD_VERT = (byte) 0xb;    // 11
    public static final byte TAR_MOTOR_LEFT      = (byte) 0x14;   // 20
    public static final byte TAR_MOTOR_RIGHT     = (byte) 0x15;   // 21
    // Commands
    public static final byte CMD_MOVE            = (byte) 0x2;
    public static final byte CMD_PAUSE           = Byte.MIN_VALUE;
    public static final byte CMD_FORWARD         = (byte) 0xa;    // 10
    public static final byte CMD_BACKWARD        = (byte) 0xb;    // 11
    public static final byte CMD_LEFT            = (byte) 0xc;    // 12
    public static final byte CMD_RIGHT           = (byte) 0xd;    // 13

}
