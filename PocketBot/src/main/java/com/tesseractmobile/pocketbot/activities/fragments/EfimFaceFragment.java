package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimFaceFragment extends FaceFragment {

    private EfimFace mRobotFace;

    @Override
    public RobotFace getRobotFace(final RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.robot_face, null);
        mRobotFace = new EfimFace(view);
        return view;
    }

}
