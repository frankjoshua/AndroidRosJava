package com.tesseractmobile.pocketbot.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

/**
 * Created by josh on 8/24/2015.
 */
public class GoogleFaceDetectActivity extends BaseFaceActivity {

    private static final String TAG = GoogleFaceDetectActivity.class.getName();
    public static final int PREVIEW_WIDTH = 640;
    public static final int PREVIEW_HEIGHT = 480;

    private CameraSource mCameraSource;
    private Handler mHandler = new Handler();
    private boolean mIsFaceDetectAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsFaceDetectAvailable = checkGooglePlayServices();
        if(mIsFaceDetectAvailable) {
            FaceDetector dectector = new FaceDetector.Builder(getApplicationContext()).build();
            dectector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());

            mCameraSource = new CameraSource.Builder(getApplicationContext(), dectector)
                    .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .build();
        }


    }

    private boolean checkGooglePlayServices() {
        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, GooglePlayServicesUtil.getErrorString(status));

            // ask user to update google play services.
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
            return false;
        } else {
            Log.i(TAG, GooglePlayServicesUtil.getErrorString(status));
            // google play services is updated.
            //your code goes here...
            return true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mIsFaceDetectAvailable) {
            try {
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsFaceDetectAvailable) {
            mCameraSource.stop();
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker();
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        @Override
        public void onNewItem(int id, Face item) {
            humanSpotted();
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            final float centerX = item.getPosition().x + item.getWidth() / 2;
            final float centerY = item.getPosition().y + item.getHeight() / 2;
            //Log.d("PocketBot", Float.toString(centerX));
            float x = centerX / PREVIEW_WIDTH;
            float y = centerY / PREVIEW_HEIGHT;
            look(2 - x * 2f, y * 2f);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
        }

        @Override
        public void onDone() {
            super.onDone();
        }
    }
}
