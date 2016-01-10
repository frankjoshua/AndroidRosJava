package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.robot.faces.ControlFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFaceFragment extends QuickBloxFragment implements View.OnClickListener, RobotSelectionDialog.OnRobotSelectedListener {

    private RobotFace mRobotFace;
    private RTCGLVideoView mRemoteVideoView;
    private Button mConnectButton;
    private RemoteState mRemoteState = RemoteState.NOT_CONNECTED;
    private QBRTCSession mSession;

    private Handler mHandler = new Handler();

    @Override
    public RobotFace getRobotFace(RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_control, null);
        mConnectButton = (Button) view.findViewById(R.id.btnConnect);
        mConnectButton.setOnClickListener(this);
        mRemoteVideoView = (RTCGLVideoView) view.findViewById(R.id.remoteVideoView);
        mRobotFace = new ControlFace(view);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnConnect){
            if(mRemoteState == RemoteState.NOT_CONNECTED){
                //Show selection dialog
                RobotSelectionDialog robotSelectionDialog = new RobotSelectionDialog();
                robotSelectionDialog.setSignInOnClickListener((View.OnClickListener) getActivity());
                robotSelectionDialog.show(getActivity().getSupportFragmentManager(), "ROBOT_SELECTION_DIALOG");
                robotSelectionDialog.setOnRobotSelectedListener(this);
            } else if (mRemoteState == RemoteState.CONNECTED){
                disconnect();
            }
        }
    }

    private void disconnect() {
        if(mSession != null){
            mSession.hangUp(null);
            setRemoteState(RemoteState.NOT_CONNECTED);
        }
        ((ControlFace) mRobotFace).setRemoteRobotId(null);
    }

    private void connectToRemoteRobot(final int remoteNumber, final String remoteRobotId) {
        final FragmentActivity activity = getActivity();
        if(activity == null){
            //Called after activity closed just return
            return;
        }
        setRemoteState(RemoteState.CONNECTING);
        List<Integer> opponents = new ArrayList<Integer>();
        opponents.add(remoteNumber); //12345 - QBUser ID

        //Set user information
        // User can set any string key and value in user info
        // Then retrieve this data from sessions which is returned in callbacks
        // and parse them as he wish
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");

        //Init session
        mSession = QBRTCClient.getInstance(activity).createNewSessionWithOpponents(opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
        newSessionCreated(mSession);
        //Start call
        mSession.startCall(userInfo);

        //Connect to remote robot
        ((ControlFace) mRobotFace).setRemoteRobotId(remoteRobotId);

        //Save UserId
        PocketBotSettings.setLastRobotId(activity, remoteRobotId);

        //Update state
        setRemoteState(RemoteState.CONNECTED);

//        //Connect to QuickBlox
//        QBUser user = new QBUser(PocketBotSettings.getRobotId(activity), PocketBotSettings.getPassword(activity));
//        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
//            @Override
//            public void onSuccess(QBSession result, Bundle params) {
//                //Initiate opponents list
//                List<Integer> opponents = new ArrayList<Integer>();
//                opponents.add(remoteNumber); //12345 - QBUser ID
//
//                //Set user information
//                // User can set any string key and value in user info
//                // Then retrieve this data from sessions which is returned in callbacks
//                // and parse them as he wish
//                Map<String, String> userInfo = new HashMap<>();
//                userInfo.put("key", "value");
//
//                //Init session
//                mSession = QBRTCClient.getInstance(activity).createNewSessionWithOpponents(opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
//
//                //Start call
//                mSession.startCall(userInfo);
//
//                //Connect to PubNub
//                ((ControlFace) mRobotFace).setRemoteRobotId(remoteRobotId);
//
//                //Save UserId
//                PocketBotSettings.setLastRobotId(activity, remoteRobotId);
//
//                //Update state
//                setRemoteState(RemoteState.CONNECTED);
//            }
//
//            @Override
//            public void onError(List<String> errors) {
//                throw new UnsupportedOperationException(errors.toString());
//            }
//        });

    }

    private void setRemoteState(RemoteState newState) {
        this.mRemoteState = newState;
        switch (mRemoteState){
            case CONNECTING:
                mConnectButton.setEnabled(false);
                mConnectButton.setText("Connecting");
                break;
            case CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText("Disconnect");
                break;
            case NOT_CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText("Connect");
                break;
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(final QBRTCSession qbrtcSession, final QBRTCVideoTrack qbrtcVideoTrack, final Integer integer) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Setup Remote video
                TelepresenceFaceFragment.fillVideoView(mRemoteVideoView, qbrtcVideoTrack, true);
                mRemoteVideoView.setVisibility(View.VISIBLE);
            }
        });;
    }

    @Override
    void onQBSetup(QBSession session, QBUser user) {
        //Do Nothing
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteVideoView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRobotSelected(RobotInfo.Settings robotinfo) {
        connectToRemoteRobot(robotinfo.prefs.qbId, robotinfo.prefs.robotId);
    }

    private enum RemoteState {
        NOT_CONNECTED, CONNECTED, CONNECTING
    }

}
