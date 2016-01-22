package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.Constants;
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
                final RobotSelectionDialog robotSelectionDialog = new RobotSelectionDialog();
                robotSelectionDialog.setOnlyUserRobots(false);
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

    }

    private void setRemoteState(final RemoteState newState) {
        this.mRemoteState = newState;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (newState){
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
        });
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
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        super.onCallAcceptByUser(qbrtcSession, integer, map);
        //Update state
        setRemoteState(RemoteState.CONNECTED);
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteVideoView.setVisibility(View.INVISIBLE);
                setRemoteState(RemoteState.NOT_CONNECTED);
            }
        });
    }

    @Override
    public void onRobotSelected(RobotInfo.Settings robotinfo) {
        if(robotinfo.prefs.robot_id.equals(PocketBotSettings.getRobotId(getContext()))){
            Toast.makeText(getContext(), "You must select a remote robot", Toast.LENGTH_LONG).show();
            //Allow if testing
            if(Constants.LOGGING == false){
                return;
            }
        }
        connectToRemoteRobot(robotinfo.prefs.quickblox_id, robotinfo.prefs.robot_id);
    }

    private enum RemoteState {
        NOT_CONNECTED, CONNECTED, CONNECTING
    }

}
