package com.tesseractmobile.pocketbot.robot;

import android.os.SystemClock;

/**
 * Created by josh on 9/13/2015.
 */
public class SensorData {
    public static final int NO_FACE = -1;

    private float face_id = NO_FACE;
    private float face_x;
    private float face_y;
    private float face_z;
    private int heading;
    private int destHeading;
    private long time;


    public void setFace_x(float face_x) {
        this.face_x = face_x;
        update();
    }

    public void setFace_y(float face_y) {
        this.face_y = face_y;
        update();
    }

    public void setFace_z(float face_z) {
        this.face_z = face_z;
        update();
    }

    public void setHeading(int heading) {
        this.heading = heading;
        update();
    }

    public void setDestHeading(int heading) {
        this.destHeading = heading;
        update();
    }

    private void update() {
        //Uptime, don't excede max int value on uno
        time = SystemClock.uptimeMillis() % 2147483647;
    }

    public float getFace_x() {
        return face_x;
    }

    public float getFace_y() {
        return face_y;
    }

    public float getFace_z() {
        return face_z;
    }

    public int getHeading() {
        return heading;
    }

    public float getFace_id() {
        return face_id;
    }

    public void setFace_id(float face_id) {
        this.face_id = face_id;
    }
}
