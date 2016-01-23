package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Created by josh on 10/18/2015.
 */
abstract public class FaceFragment extends CallbackFragment {

    public static final int ID_FACE_EFIM = 0;

    abstract public RobotFace getRobotFace(final RobotInterface robotInterface);

}
