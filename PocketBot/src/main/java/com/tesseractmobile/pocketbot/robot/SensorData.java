package com.tesseractmobile.pocketbot.robot;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

/**
 * Data in this class is sent to the Arduino
 * It is all converted to JSON so unused fields will still be accessed
 *
 * Created by josh on 9/13/2015.
 */
public class SensorData {

    /** Face left or no face detected */
    public static final int NO_FACE = -1;

    private SensorData.Sensor sensor = new SensorData.Sensor();
    private SensorData.Face face = new SensorData.Face();
    private SensorData.Control control = new SensorData.Control();

    //private long time;
    public void setFace(int id) {
        face.id = id;
        update();
    }

    public void setFace(float x, float y, float z) {
        face.X = x;
        face.Y = y;
        face.Z = z;
        update();
    }

    public void setHeading(int heading) {
        sensor.heading = heading;
        update();
    }

    private void update() {
        //Uptime, don't excede max int value on uno
        //time = SystemClock.uptimeMillis() % 2147483647;
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
        sensor.proximity = proximity;
        update();
    }

    public void setJoystick1(final float x, final float y, final float z) {
        control.joy1.X = round(x, 2);
        control.joy1.X = round(y, 2);
        control.joy1.Z = round(z, 2);
        update();
    }

    public void setJoystick1(final float x, final float y, final float z, final boolean a, final boolean b, final int heading) {
        setJoystick1(x, y, z);
        control.joy1.A = a;
        control.joy1.B = b;
        control.joy1.heading = heading;
        update();
    }

    public void setJoystick2(final float x, final float y, final float z, final boolean a, final boolean b, final int heading) {
        control.joy2.X = round(x, 2);
        control.joy2.X = round(y, 2);
        control.joy2.Z = round(z, 2);
        control.joy2.A = a;
        control.joy2.B = b;
        control.joy2.heading = heading;
        update();
    }


    public static PocketBotProtocol.PocketBotMessage toPocketBotMessage(final SensorData sensorData) {
        //Builders
        final PocketBotProtocol.PocketBotMessage.Builder messageBuilder = PocketBotProtocol.PocketBotMessage.newBuilder();
        final PocketBotProtocol.Face.Builder faceBuilder = PocketBotProtocol.Face.newBuilder();
        final PocketBotProtocol.Control.Builder controlBuilder = PocketBotProtocol.Control.newBuilder();
        final PocketBotProtocol.Sensor.Builder sensorBuilder = PocketBotProtocol.Sensor.newBuilder();
        final PocketBotProtocol.Gps.Builder gpsBuilder = PocketBotProtocol.Gps.newBuilder();
        final PocketBotProtocol.Joystick.Builder joyStickBuilder = PocketBotProtocol.Joystick.newBuilder();

        //Objects
        final PocketBotProtocol.Face face = faceBuilder
                .setId(sensorData.getFace().id)
                .setX(sensorData.getFace().X)
                .setY(sensorData.getFace().Y)
                .setZ(sensorData.getFace().Z)
                .build();
        final PocketBotProtocol.Joystick joy1 = joyStickBuilder
                .setA(sensorData.getControl().joy1.A)
                .setB(sensorData.getControl().joy1.B)
                .setHeading(sensorData.getControl().joy1.heading)
                .setX(sensorData.getControl().joy1.X)
                .setY(sensorData.getControl().joy1.Y)
                .setZ(sensorData.getControl().joy1.Z)
                .build();
        final PocketBotProtocol.Joystick joy2 = joyStickBuilder
                .setA(sensorData.getControl().joy2.A)
                .setB(sensorData.getControl().joy2.B)
                .setHeading(sensorData.getControl().joy2.heading)
                .setX(sensorData.getControl().joy2.X)
                .setY(sensorData.getControl().joy2.Y)
                .setZ(sensorData.getControl().joy2.Z)
                .build();
        final PocketBotProtocol.Control control = controlBuilder
                .setJoy1(joy1)
                .setJoy2(joy2)
                .build();
        final PocketBotProtocol.Gps gps = gpsBuilder
                .setLat(sensorData.getSensor().gps.lat)
                .setLon(sensorData.getSensor().gps.lon)
                .build();
        final PocketBotProtocol.Sensor sensor = sensorBuilder
                .setHeading(sensorData.getSensor().heading)
                .setProximity(sensorData.getSensor().proximity)
                .setGps(gps)
                .build();

        //Message
        final PocketBotProtocol.PocketBotMessage pocketBotMessage = messageBuilder
                .setFace(face)
                .setControl(control)
                .setSensor(sensor)
                .build();

        return pocketBotMessage;
    }

    public Control getControl() {
        return control;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Face getFace() {
        return face;
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

    static public class Gps {
        public float lat;
        public float lon;
    }

    static public class Joystick {
        public boolean A;
        public boolean B;
        public int heading;
        public float X;
        public float Y;
        public float Z;
    }

    static public class Face {
        public int id = NO_FACE;
        public float X;
        public float Y;
        public float Z;
    }

    static public class Sensor {
        public int heading;
        public boolean proximity;
        public SensorData.Gps gps = new SensorData.Gps();
    }

    static public class Control {
        public SensorData.Joystick joy1 = new SensorData.Joystick();
        public SensorData.Joystick joy2 = new SensorData.Joystick();
    }
}
