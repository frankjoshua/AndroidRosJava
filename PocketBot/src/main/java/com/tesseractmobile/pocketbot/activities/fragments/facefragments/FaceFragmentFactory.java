package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 1/24/2016.
 */
public class FaceFragmentFactory {

    public static final int ID_FACE_EFIM = 0;
    public static final int ID_FACE_CONTROL = 1;
    public static final int ID_FACE_TELEPRESENCE = 2;
    public static final int ID_FACE_TELEPRESENCE_EFIM = 3;

    public static FaceFragment getFaceFragment(final int faceId) {
        final FaceFragment faceFragment;
        switch (faceId){
            case ID_FACE_EFIM:
                faceFragment = new EfimFaceFragment();
                break;
            case ID_FACE_CONTROL:
                faceFragment = new ControlFaceFragment();
                break;
            case ID_FACE_TELEPRESENCE:
                faceFragment = new TelepresenceFaceFragment();
                break;
            case ID_FACE_TELEPRESENCE_EFIM:
                faceFragment = new EfimTelepresenceFaceFragment();
                break;
            default:
                throw new UnsupportedOperationException("Unknown face id " + faceId);
        }
        return faceFragment;
    }

    public static List<FaceInfo> getFaceInfoList() {
        final ArrayList<FaceInfo> faceList = new ArrayList<>();
        faceList.add(new FaceInfo(ID_FACE_EFIM, "Robot"));
        faceList.add(new FaceInfo(ID_FACE_CONTROL, "Control"));
        faceList.add(new FaceInfo(ID_FACE_TELEPRESENCE, "Telepresence"));
        faceList.add(new FaceInfo(ID_FACE_TELEPRESENCE_EFIM, "Remote Robot"));
        return faceList;
    }
}
