package com.tesseractmobile.opencv.views;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class FixedJavaCameraView extends JavaCameraView {

    public FixedJavaCameraView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        
    }

    @Override
    public void surfaceChanged(final SurfaceHolder arg0, final int arg1, final int arg2, final int arg3) {
//        final Camera.Parameters parameters = mCamera.getParameters();
//        final List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//
//        // You need to choose the most appropriate previewSize for your app
//        final Camera.Size previewSize = previewSizes.get(0);// .... select one of previewSizes here
//
//        parameters.setPreviewSize(previewSize.width, previewSize.height);
//        mCamera.setParameters(parameters);
//        mCamera.startPreview();
        super.surfaceChanged(arg0, arg1, arg2, arg3);
    }

}
