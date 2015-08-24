package com.tesseractmobile.pocketbot.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    private CameraSource mCameraSource;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FaceDetector dectector = new FaceDetector.Builder(getApplicationContext()).build();
        dectector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());
        
        mCameraSource = new CameraSource.Builder(getApplicationContext(), dectector)
                .setRequestedPreviewSize(640,480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraSource.stop();
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
            Log.d("PocketBot", Float.toString(item.getPosition().x));
            float x =  (480 / 2) / item.getPosition().x;
            float y = item.getPosition().y / (640 / 2);
            look((x - 1) * 0.5f + 1, (y - 1) * 0.5f + 1);
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
