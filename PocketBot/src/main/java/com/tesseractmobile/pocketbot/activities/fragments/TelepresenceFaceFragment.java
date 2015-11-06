package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.faces.ControlFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

import org.json.JSONObject;
import org.webrtc.VideoRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 10/31/2015.
 */
public class TelepresenceFaceFragment extends QuickBloxFragment {


    private RobotFace mRobotFace;
    private QBGLVideoView mRemoteVideoView;
    private TextView mUserId;

    @Override
    public RobotFace getRobotFace(RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_telepresence, null);
        mRobotFace = new TelePresenceFace(view);
        mRemoteVideoView = (QBGLVideoView) view.findViewById(R.id.remoteVideoView);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT"); //Test

    }

    @Override
    protected void onQBSetup(final QBSession session, final QBUser user) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserId.setText(Integer.toString(session.getUserId()));
            }
        });
    }

    @Override
    public void onReceiveNewSession(final QBRTCSession qbrtcSession) {
        // Set userInfo
        // User can set any string key and value in user info
        // Then retrieve this data from sessions which is returned in callbacks
        // and parse them as he wish
        final Map<String,String> userInfo = new HashMap<String,String>();
        userInfo.put("Key", "Value");

        // Accept incoming call
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                qbrtcSession.acceptCall(userInfo);
            }
        });

        //Subscribe to PubNub
        try {
            pubnub.subscribe(mUserId.getText().toString(), new Callback() {
                @Override
                public void errorCallback(String channel, PubnubError error) {
                    throw new UnsupportedOperationException(error.getErrorString());
                }

                @Override
                public void successCallback(String channel, final Object message, String timetoken) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final JSONObject jsonObject = (JSONObject) message;
                            mUserId.setText(jsonObject.toString());
                            ((TelePresenceFace) mRobotFace).sendJson(jsonObject);
                        }
                    });
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        //Setup Remote video
        VideoRenderer remoteRenderer = new VideoRenderer(new VideoCallBacks(mRemoteVideoView, QBGLVideoView.Endpoint.REMOTE));
        qbrtcVideoTrack.addRenderer(remoteRenderer);
        mRemoteVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.REMOTE);
    }
}
