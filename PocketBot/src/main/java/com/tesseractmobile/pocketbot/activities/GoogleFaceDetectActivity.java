package com.tesseractmobile.pocketbot.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.views.CameraSourcePreview;
import com.tesseractmobile.pocketbot.views.GraphicOverlay;
import com.tesseractmobile.pocketbot.views.FaceGraphic;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by josh on 8/24/2015.
 */
public class GoogleFaceDetectActivity extends BaseFaceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = GoogleFaceDetectActivity.class.getName();
    public int PREVIEW_WIDTH = 240;
    public int PREVIEW_HEIGHT = 320;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private Handler mHandler = new Handler();
    private boolean mIsFaceDetectAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PREVIEW_HEIGHT = (int) getResources().getDimension(R.dimen.height_camera_preview);
        PREVIEW_WIDTH = (int) getResources().getDimension(R.dimen.width_camera_preview);

        mIsFaceDetectAvailable = checkGooglePlayServices();
        if(mIsFaceDetectAvailable) {
            FaceDetector dectector = new FaceDetector.Builder(getApplicationContext())
                    .setTrackingEnabled(true)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .build();
            dectector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());

            mCameraSource = new CameraSource.Builder(getApplicationContext(), dectector)
                    .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(20.0f)
                    .build();



        }

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        if(PocketBotSettings.isShowPreview(this)){
            mPreview.setVisibility(View.VISIBLE);
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
                //mCameraSource.start();
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        PocketBotSettings.registerOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsFaceDetectAvailable) {
            mCameraSource.stop();
        }
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PocketBotSettings.SHOW_PREVIEW)){
            if(sharedPreferences.getBoolean(key, false)){
                mPreview.setVisibility(View.VISIBLE);
            } else {
                mPreview.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    static public XYZ getCenter(final Face face, final int viewWidth, final int viewHeight){
        final XYZ xyz = new XYZ();
        //Center horizontal
        final float centerX = face.getPosition().x + face.getWidth() / 2;
        //Above center for vertical (Look into eyes instead of face)
        final float centerY = face.getPosition().y + face.getHeight() / 2;
        //Log.d("PocketBot", Float.toString(centerX));
        float cx = centerX / viewWidth;
        float cy = centerY / viewHeight;

        xyz.x = 2 - cx * 2f;
        xyz.y = cy * 2f;
        xyz.z = face.getHeight() / viewHeight;

        return xyz;
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int id, Face item) {
            mFaceGraphic.setId(id);
            humanSpotted();
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(item);

            final XYZ xyz = GoogleFaceDetectActivity.getCenter(item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            look(xyz.x, xyz.y, xyz.z);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            mOverlay.remove(mFaceGraphic);
            look(1.0f, 1.0f, 1.0f);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            super.onDone();
        }
    }

    public static class XYZ {
        public float x,y,z;
    }
}
