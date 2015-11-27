package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
    private Button mConnectButton;
    private RemoteState mRemoteState = RemoteState.NOT_CONNECTED;
    private QBRTCSession mSession;


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
        mRemoteVideoView = (QBGLVideoView) view.findViewById(R.id.remoteVideoView);
        mRemoteUserId = (EditText) view.findViewById(R.id.edUserId);
        mRobotFace = new ControlFace(view);
        //Set last user id
        mRemoteUserId.setText(PocketBotSettings.getLastUserId(getActivity()));
        //Listen for done button
        mRemoteUserId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    connectToRemoteRobot();
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnConnect){
            if(mRemoteState == RemoteState.NOT_CONNECTED){
                connectToRemoteRobot();
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
        ((ControlFace) mRobotFace).setPubNub(null, null);
    }

    private void connectToRemoteRobot() {
        setRemoteState(RemoteState.CONNECTING);
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
                mSession = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);

                //Start call
                mSession.startCall(userInfo);

                //Connect to PubNub
                ((ControlFace) mRobotFace).setPubNub(pubnub, mRemoteUserId.getText().toString());

                //Save UserId
                PocketBotSettings.setLastUserId(getActivity(), mRemoteUserId.getText().toString());

                //Update state
                setRemoteState(RemoteState.CONNECTED);
            }

            @Override
            public void onError(List<String> errors) {
                throw new UnsupportedOperationException(errors.toString());
            }
        });

    }

    private void setRemoteState(RemoteState newState) {
        this.mRemoteState = newState;
        switch (mRemoteState){
            case CONNECTING:
                mConnectButton.setEnabled(false);
                mConnectButton.setText("Connecting");
                mRemoteUserId.setVisibility(View.INVISIBLE);
                break;
            case CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText("Disconnect");
                mRemoteUserId.setVisibility(View.INVISIBLE);
                break;
            case NOT_CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText("Connect");
                mRemoteUserId.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        //Setup Remote video
        VideoRenderer remoteRenderer = new VideoRenderer(new VideoCallBacks(mRemoteVideoView, QBGLVideoView.Endpoint.REMOTE));
        qbrtcVideoTrack.addRenderer(remoteRenderer);
        mRemoteVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.REMOTE);
    }

    private enum RemoteState {
        NOT_CONNECTED, CONNECTED, CONNECTING
    }
}
