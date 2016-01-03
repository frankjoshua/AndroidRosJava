package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.RemoteListener;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimTelepresenceFaceFragment extends QuickBloxFragment implements RemoteListener{

    private static final String TAG = EfimTelepresenceFaceFragment.class.getSimpleName();
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

    @Override
    public void onReceiveNewSession(final QBRTCSession qbrtcSession) {
        Log.d(TAG, "WevRTC session received");
        // Set userInfo
        // User can set any string key and value in user info
        // Then retrieve this data from sessions which is returned in callbacks
        // and parse them as he wish


        // Accept incoming call
        final FragmentActivity activity = getActivity();
        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Map<String,String> userInfo = new HashMap<String,String>();
                    userInfo.put("Key", "Value");
                    qbrtcSession.acceptCall(userInfo);
                }
            });
        } else {
            Log.e(TAG, "Activity is null!");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //Listen to remote messages
        RemoteControl.get().registerRemoteListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening to remote messages
        RemoteControl.get().unregisterRemoteListener(this);
    }


    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }

    @Override
    void onQBSetup(QBSession session, QBUser user) {
    }

    @Override
    public void onMessageReceived(Object message) {
        ((EfimFace) mRobotFace).sendJson((JSONObject) message);

    }
}
