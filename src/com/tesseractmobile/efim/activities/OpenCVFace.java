package com.tesseractmobile.efim.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.facedetect.DetectionBasedTracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tesseractmobile.efim.R;


public class OpenCVFace extends BaseFaceActivity implements CvCameraViewListener2{

    private static final String TAG = "OpenCVFace";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private CameraBridgeViewBase mOpenCvCameraView;
    private DetectionBasedTracker mNativeDetector;
    private final BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(final int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        final InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        final File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        final File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        final FileOutputStream os = new FileOutputStream(mCascadeFile);

                        final byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

//                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//                        if (mJavaDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
//                            mJavaDetector = null;
//                        } else
//                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
//
                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (final IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private View mViewBlocker;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnClickListener(this);
        mViewBlocker = findViewById(R.id.viewCameraBlocker);      
        mViewBlocker.setOnClickListener(this);        
    }
    
    
    
    @Override
    public void onClick(final View v) {
        if(mViewBlocker.getVisibility() != View.INVISIBLE){
            mViewBlocker.setVisibility(View.INVISIBLE);
        } else {
            mViewBlocker.setVisibility(View.VISIBLE);
        }
        super.onClick(v);
    }



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    
    
    @Override
    public void onCameraViewStarted(final int width, final int height) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCameraViewStopped() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
        //final Mat mRgba = inputFrame.rgba();
        final Mat mGray = inputFrame.gray();
//        final Mat mGrayT = mGray.t();
//        Core.flip(mGray.t(), mGrayT, 0);
//        Imgproc.resize(mGrayT, mGrayT, mGray.size());
        
        final int height = mGray.rows();
        int mAbsoluteFaceSize = 0;
        if (Math.round(height * 0.2f) > 0) {
            mAbsoluteFaceSize = Math.round(height * 0.2f);
        }
        mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);


        final MatOfRect faces = new MatOfRect();

        if (mNativeDetector != null)
            mNativeDetector.detect(mGray, faces);

        final boolean emotionChanged = false;
        final Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++){
            final Point tl = facesArray[i].tl();
            final Point br = facesArray[i].br();
            Imgproc.rectangle(mGray, tl, facesArray[i].br(), FACE_RECT_COLOR, 3);
//            if(tl.y < 100){
//                setEmotion(Emotion.ANGER);
//                emotionChanged = true;
//            }
            final double rectWidth = br.x - tl.x;
            final double rectHeight = br.y - tl.y;
            final double rectCenterX = tl.x + rectWidth / 2;
            final double rectCenterY = tl.y + rectHeight / 2;
            final float x = (float) ((mGray.width() / 2) / rectCenterX);
            final float y = (float) rectCenterY / (mGray.height() / 2);
            look((x - 1) * 0.5f + 1, (y - 1) * 0.5f + 1); 
            //look(x,y); 
        }
        
        if(emotionChanged == false){
            if(facesArray.length > 0){
                setEmotion(Emotion.JOY);
            } else {
                setEmotion(Emotion.FEAR);
            }
        }
        
        return mGray;
    }


}
