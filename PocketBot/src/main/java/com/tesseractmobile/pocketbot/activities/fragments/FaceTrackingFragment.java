package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.views.CameraSourcePreview;
import com.tesseractmobile.pocketbot.views.FaceGraphic;
import com.tesseractmobile.pocketbot.views.GraphicOverlay;

import java.io.IOException;

/**
 * Created by josh on 10/18/2015.
 */
public class FaceTrackingFragment extends CallbackFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public int PREVIEW_WIDTH = 240;
    public int PREVIEW_HEIGHT = 320;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private RobotInterface mRobotInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        PREVIEW_HEIGHT = (int) getResources().getDimension(R.dimen.height_camera_preview);
        PREVIEW_WIDTH = (int) getResources().getDimension(R.dimen.width_camera_preview);

        FaceDetector dectector = new FaceDetector.Builder(activity.getApplicationContext())
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        dectector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());

        mCameraSource = new CameraSource.Builder(activity.getApplicationContext(), dectector)
                .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.camera_preview, container, false);
        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.faceOverlay);
        if(PocketBotSettings.isShowPreview(getActivity())){
            mPreview.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            //Log.e(TAG, e.getMessage());
        }
        PocketBotSettings.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mCameraSource.stop();
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    public void setRobotInterface(RobotInterface mRobotInterface) {
        this.mRobotInterface = mRobotInterface;
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
            mRobotInterface.humanSpotted(id);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            final XYZ xyz = FaceTrackingFragment.getCenter(item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mFaceGraphic.setmXyz(xyz);
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(item);
            mRobotInterface.look(xyz.x, xyz.y, xyz.z);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            mOverlay.remove(mFaceGraphic);

        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            mRobotInterface.humanSpotted(-1);
            mRobotInterface.look(1.0f, 1.0f, 1.0f);
            super.onDone();
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

    public static class XYZ {
        public float x,y,z;
    }
}
