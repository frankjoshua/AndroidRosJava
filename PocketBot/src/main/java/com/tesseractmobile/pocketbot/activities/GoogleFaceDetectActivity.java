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


    private Handler mHandler = new Handler();
    private boolean mIsFaceDetectAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsFaceDetectAvailable = checkGooglePlayServices();
        if(mIsFaceDetectAvailable) {

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

        }
        PocketBotSettings.registerOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsFaceDetectAvailable) {

        }
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PocketBotSettings.SHOW_PREVIEW)){
//            if(sharedPreferences.getBoolean(key, false)){
//                mPreview.setVisibility(View.VISIBLE);
//            } else {
//                mPreview.setVisibility(View.INVISIBLE);
//            }
        }
    }




}
