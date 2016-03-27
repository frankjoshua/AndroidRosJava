package com.tesseractmobile.pocketbot.robot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;

import com.tesseractmobile.pocketbot.activities.BaseFaceFragmentActivity;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Receives input from Android sensors, cleans input and sends to robot interface
 * Created by josh on 3/27/2016.
 */
public class SensorControler implements SensorEventListener {
    //Device sensor manager
    private SensorManager mSensorManager;

    //Storage for sensors
    static private float ROTATION[] = new float[9];
    static private float INCLINATION[] = new float[9];
    static private float ORIENTATION[] = new float[3];

    private float[] mGravity;
    private float[] mGeomagnetic;
    /** Used to send sensor data */
    private RobotInterface mRobotInterFace;
    /** For battery level updates */
    private BroadcastReceiver mBatteryReceiver;

    public SensorControler(final Context context) {
        //Start senors
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void onResume(final Context context, final RobotInterface robotInterface) {
        //Set the robot interface
        mRobotInterFace = robotInterface;
        //Start listening for orientation
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        //Listen to proximity sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);

        //Listen for battery status
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBatteryReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                //information about battery status
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level / (float)scale;
                SensorData sensorData = mRobotInterFace.getSensorData();
                sensorData.getSensor().battery = (int) (batteryPct * 100);
            }
        };
        context.registerReceiver(mBatteryReceiver, filter);
    }

    public void onPause(final Context context) {
        //Stop listening for orientation
        mSensorManager.unregisterListener(this);
        mRobotInterFace = null;
        //Stop listening for battery status
        context.unregisterReceiver(mBatteryReceiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //mGravity = lowPass(event.values.clone(), mGravity);
            mGravity = lowPass(event.values, mGravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
            mGeomagnetic = lowPass(event.values, mGeomagnetic);
        }
        SensorData sensorData = mRobotInterFace.getSensorData();
        if (mGravity != null && mGeomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(ROTATION, INCLINATION, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(ROTATION, ORIENTATION);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                final int heading = (int) (Math.toDegrees(ORIENTATION[0]) + 360 + 180) % 360;
                if (Math.abs(heading - sensorData.getSensor().heading) > 1) {
                    sensorData.setHeading(heading);
                    mRobotInterFace.sendSensorData(false);
                    //Log.d(TAG, " New Heading " + heading);
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            final float distance = event.values[0];
            //Distance is either touching or not
            sensorData.setProximity(distance < 1.0f);
            mRobotInterFace.sendSensorData(true);
            //Log.d(TAG, "Proximity " + Float.toString(distance));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.25f * (input[i] - output[i]);
        }
        return output;
    }
}
