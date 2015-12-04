package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

/**
 * Created by josh on 12/2/2015.
 */
public class LockedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view;

        view = inflater.inflate(R.layout.locked_service, null);

        return view;
    }
}
