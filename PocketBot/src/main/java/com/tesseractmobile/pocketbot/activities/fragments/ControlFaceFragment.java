package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.faces.ControlFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFaceFragment extends QuickBloxFragment implements View.OnClickListener {

    private RobotFace mRobotFace;
    private EditText mRemoteUserId;
    private QBGLVideoView mRemoteVideoView;


    @Override
    public RobotFace getRobotFace(RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_control, null);
        view.findViewById(R.id.btnConnect).setOnClickListener(this);
        mRemoteVideoView = (QBGLVideoView) view.findViewById(R.id.remoteVideoView);
        mRemoteUserId = (EditText) view.findViewById(R.id.edUserId);
        mRobotFace = new ControlFace(view);
        return view;
    }

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnConnect){
            connectToRemoteRobot();
        }
    }

    private void connectToRemoteRobot() {
        //Connect to QuickBlox
        QBUser user = new QBUser(PocketBotSettings.getUserName(getActivity()), PocketBotSettings.getPassword(getActivity()));
        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                //Initiate opponents list
                List<Integer> opponents = new ArrayList<Integer>();
                opponents.add(Integer.parseInt(mRemoteUserId.getText().toString())); //12345 - QBUser ID

                //Set user information
                // User can set any string key and value in user info
                // Then retrieve this data from sessions which is returned in callbacks
                // and parse them as he wish
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("key", "value");

                //Init session
                QBRTCSession session =
                        QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);

                //Start call
                session.startCall(userInfo);

                //Connect to PubNub
                ((ControlFace) mRobotFace).setPubNub(pubnub, mRemoteUserId.getText().toString());
            }

            @Override
            public void onError(List<String> errors) {
                throw new UnsupportedOperationException(errors.toString());
            }
        });

    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        //Setup Remote video
        VideoRenderer remoteRenderer = new VideoRenderer(new VideoCallBacks(mRemoteVideoView, QBGLVideoView.Endpoint.REMOTE));
        qbrtcVideoTrack.addRenderer(remoteRenderer);
        mRemoteVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.REMOTE);
    }
}
