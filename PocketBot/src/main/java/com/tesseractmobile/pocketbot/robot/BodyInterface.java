package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 8/29/2015.
 */
public interface BodyInterface {
    /*
    * Converts an object to JSON then sends to the robot body
     */
    public void sendObject(final Object object);

    /**
     *
     * @return true is body is connected
     */
    public boolean isConnected();

    /**
     * Send JSON directly
     * @param json
     */
    public void sendJson(String json);
}
