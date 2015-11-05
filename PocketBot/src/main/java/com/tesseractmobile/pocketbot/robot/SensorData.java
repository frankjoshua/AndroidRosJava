package com.tesseractmobile.pocketbot.robot;

import java.math.BigDecimal;

/**
 * Data in this class is sent to the Arduino
 * It is all converted to JSON so unused fields will still be accessed
 *
 * Created by josh on 9/13/2015.
 */
public class SensorData {

    /** Face left or no face detected */
    public static final int NO_FACE = -1;

    /** FACE_ID */
    private int f = NO_FACE;
    /** FACE_X */
    private float fx;
    /** FACE_Y */
    private float fy;
    /** FACE_Z */
    private float fz;
    /** HEADING */
    private int h;
    /** HEADING_DESTINATION */
    private int dh;
    /** PROXIMITY */
    private boolean p;
    /** JOYSTICK_X */
    private float jx;
    /** JOYSTICK_Y */
    private float jy;
    /** JOYSTICK_Z */
    private float jz;
    private boolean mButtonA;
    private boolean mButtonB;
    private float mLat;
    private float mLon;
    //private long time;


    public void setFace_x(float face_x) {
        this.fx = round(face_x, 2);
        update();
    }

    public void setFace_y(float face_y) {
        this.fy = round(face_y, 2);
        update();
    }

    public void setFace_z(float face_z) {
        this.fz = round(face_z, 2);
        update();
    }

    public void setHeading(int heading) {
        this.h = heading;
        update();
    }

    public void setDestHeading(int heading) {
        this.dh = heading;
        update();
    }

    private void update() {
        //Uptime, don't excede max int value on uno
        //time = SystemClock.uptimeMillis() % 2147483647;
    }

    public float getFace_x() {
        return fx;
    }

    public float getFace_y() {
        return fy;
    }

    public float getFace_z() {
        return fz;
    }

    public int getHeading() {
        return h;
    }

    public int getFace_id() {
        return f;
    }

    public void setFace_id(int face_id) {
        this.f = face_id;
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace the numbers of decimals
     * @return
     */
    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public void setProximity(boolean proximity) {
        this.p = proximity;
        update();
    }

    public void setJoystick(float x, float y, float z) {
        this.jx = round(x, 2);
        this.jy = round(y, 2);
        this.jz = round(z, 2);
        update();
    }

    public float getJoyX() {
        return jx;
    }

    public float getJoyY() {
        return jy;
    }

    public float getJoyZ() {
        return jz;
    }

    private boolean getProximity() {
        return p;
    }

    public static PocketBotProtocol.PocketBotMessage toPocketBotMessage(SensorData sensorData) {
        //Builders
        final PocketBotProtocol.PocketBotMessage.Builder messageBuilder = PocketBotProtocol.PocketBotMessage.newBuilder();
        final PocketBotProtocol.Face.Builder faceBuilder = PocketBotProtocol.Face.newBuilder();
        final PocketBotProtocol.Control.Builder controlBuilder = PocketBotProtocol.Control.newBuilder();
        final PocketBotProtocol.Sensor.Builder sensorBuilder = PocketBotProtocol.Sensor.newBuilder();
        final PocketBotProtocol.Gps.Builder gpsBuilder = PocketBotProtocol.Gps.newBuilder();
        //Objects
        final PocketBotProtocol.Face face = faceBuilder
                .setFaceId(sensorData.getFace_id())
                .setFaceX(sensorData.getFace_x())
                .setFaceY(sensorData.getFace_y())
                .setFaceZ(sensorData.getFace_z())
                .build();
        final PocketBotProtocol.Control control = controlBuilder
                .setJoyX(sensorData.getJoyX())
                .setJoyY(sensorData.getJoyY())
                .setJoyZ(sensorData.getJoyZ())
                .setButtonA(sensorData.getButtonA())
                .setButtonB(sensorData.getButtonB())
                .setDestHeading(sensorData.getDestHeading())
                .build();
        final PocketBotProtocol.Sensor sensor = sensorBuilder
                .setHeading(sensorData.getHeading())
                .setProximity(sensorData.getProximity())
                .build();
        final PocketBotProtocol.Gps gps = gpsBuilder
                .setLat(sensorData.getLat())
                .setLon(sensorData.getLon())
                .build();
        //Message
        final PocketBotProtocol.PocketBotMessage pocketBotMessage = messageBuilder
                .setFace(face)
                .setControl(control)
                .setSensor(sensor)
                .setGps(gps)
                .build();
        return pocketBotMessage;
    }

    private float getLat() {
        return mLat;
    }

    private float getLon() {
        return mLon;
    }

    private int getDestHeading() {
        return dh;
    }

    private boolean getButtonA() {
        return mButtonA;
    }

    private boolean getButtonB() {
        return mButtonB;
    }

    /**
     * Wrap message in the format that is expected by the Arduino
     * @param data
     * @return
     */
    public static byte[] wrapData(byte[] data) {
        //Create message to be sent
        final byte[] message = new byte[data.length + 2];
        //Add start byte
        message[0] = (byte) CommandContract.START_BYTE;
        //Add data
        System.arraycopy(data, 0, message, 1, data.length);
        //Add stop byte
        message[message.length - 1] = (byte) CommandContract.STOP_BYTE;
        return message;
    }
}
