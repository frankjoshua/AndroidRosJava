package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.RemoteListener;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

import org.json.JSONObject;
import org.webrtc.VideoRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by josh on 10/31/2015.
 */
public class TelepresenceFaceFragment extends QuickBloxFragment implements RemoteListener {


    private RobotFace mRobotFace;
    private QBGLVideoView mRemoteVideoView;
    private TextView mUserId;
    //private String mChannel;

    @Override
    public RobotFace getRobotFace(RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view;
        view = inflater.inflate(R.layout.face_telepresence, null);
        mRemoteVideoView = (QBGLVideoView) view.findViewById(R.id.remoteVideoView);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
        mRobotFace = new TelePresenceFace(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT"); //Test

    }

    @Override
    protected void onQBSetup(final QBSession session, final QBUser user) {
        final Integer userId = session.getUserId();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserId.setText(Integer.toString(userId));
            }
        });
        PocketBotSettings.setQuickBloxId(getActivity(), userId);
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

        //Listen for remote messages
        RemoteControl.get().registerRemoteListener(this);

    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        //Setup Remote video
        VideoRenderer remoteRenderer = new VideoRenderer(new VideoCallBacks(mRemoteVideoView, QBGLVideoView.Endpoint.REMOTE));
        qbrtcVideoTrack.addRenderer(remoteRenderer);
        mRemoteVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.REMOTE);
    }

    @Override
    public void onMessageReceived(Object message) {
        ((TelePresenceFace) mRobotFace).sendJson((JSONObject) message);
    }
}
