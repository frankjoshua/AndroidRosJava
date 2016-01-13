package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.camera_preview, container, false);
        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.faceOverlay);
        updateView(PocketBotSettings.isShowPreview(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startFaceDetection();
        //Listen for settings changes
        PocketBotSettings.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    private void startFaceDetection() {
        final FaceDetector detector;
        if(PocketBotSettings.getFastTrackingMode(getActivity())) {
            detector = new FaceDetector.Builder(getActivity().getApplicationContext())
                    .setTrackingEnabled(true)
                    .setMode(FaceDetector.FAST_MODE)
                    .setProminentFaceOnly(true)
                    .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                    .setLandmarkType(FaceDetector.NO_LANDMARKS)
                    .build();

        } else {
            detector = new FaceDetector.Builder(getActivity().getApplicationContext())
                    .setTrackingEnabled(true)
                    .setMode(FaceDetector.ACCURATE_MODE)
                    .setProminentFaceOnly(true)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();
        }
        detector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());
//        detector.setProcessor(
//                new LargestFaceFocusingProcessor(
//                        detector,
//                        new GraphicFaceTracker(mGraphicOverlay)));

        mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), detector)
                .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(15.0f)
                .build();

        try {
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }
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
        if(key.equals(PocketBotSettings.KEY_SHOW_PREVIEW)){
            final boolean showPreview = sharedPreferences.getBoolean(key, false);
            updateView(showPreview);
        } else if(key.equals(PocketBotSettings.KEY_FAST_TRACKING)){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCameraSource.stop();
                    startFaceDetection();
                }
            });
        }
    }

    private void updateView(boolean showPreview) {
        if(showPreview){
            mPreview.setVisibility(View.VISIBLE);
        } else {
            mPreview.setVisibility(View.INVISIBLE);
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
        final XYZ xyz = new XYZ();

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int id, Face item) {
            FaceTrackingFragment.getCenter(xyz, item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mFaceGraphic.setmXyz(xyz);
            mFaceGraphic.setId(id);
            mRobotInterface.humanSpotted(id);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            //Face position has changed
            FaceTrackingFragment.getCenter(xyz, item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(item);
            mRobotInterface.look(xyz.x, xyz.y, xyz.z);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            //Face missed for a frame or more
            mOverlay.remove(mFaceGraphic);
            mRobotInterface.look(1.0f, 1.0f, 1.0f);
            mRobotInterface.humanSpotted(-1);
        }

        @Override
        public void onDone() {
            //Called when face is lost
            super.onDone();
        }
    }

    /**
     * Updates XYZ with current face info
     * @param xyz
     * @param face
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    static public XYZ getCenter(XYZ xyz, final Face face, final int viewWidth, final int viewHeight){
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
