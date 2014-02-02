package com.tesseractmobile.efim.robot;

public interface RobotCommandInterface {
    
    /**
     * Return true if successful
     * @param command
     * @param target
     * @param value
     * @return
     */
    public boolean sendCommand(final byte command, final byte target, int value);
    /**
     * Return true if successful
     * @param command
     * @param target
     * @param value
     * @return
     */
    public boolean sendCommand(final RobotCommand robotCommand);
    public void registerRobotEventListener(final RobotEventListener robotEventListener);
    public void unregisterRobotEventListener(final RobotEventListener robotEventListener);
    
    public void reconnectRobot();
    
}
