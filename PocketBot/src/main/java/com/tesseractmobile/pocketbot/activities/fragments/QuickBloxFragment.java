package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 11/1/2015.
 */
abstract public class QuickBloxFragment extends FaceFragment implements QBRTCClientSessionCallbacks, QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback {

    private QBChatService chatService;
    private QBRTCSession mCurrentRTCSession;
    private Handler mHandler = new Handler();

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        //Check if signed in
        if(PocketBotSettings.isSignedIn(context)){

        } else {

        }

        //Sign in user
        signIn(context);
    }

    protected void signIn(final Context context) {
        //Get the username and password
        final String login = PocketBotSettings.getRobotId(context);
        final String password = PocketBotSettings.getPassword(context);
        //Create a user
        final QBUser user = new QBUser(login, password);
        //Create a session
        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                //Save the id so other robots can use it to call
                PocketBotSettings.setQuickBloxId(context, session.getUserId());

                //After signin continue to set up call
                setUpQB(session, user, context);
            }

            @Override
            public void onError(List<String> errors) {
                //error
                if (errors.get(0).equals("Unauthorized")) {
                    //User has no account, so sign them up
                    signUpUser(user, context);
                } else {
                    //Unhandled Error
                    error(errors.toString());
                }
            }
        });
    }

    /**
     * Creates a new user account
     * @param user
     */
    private void signUpUser(final QBUser user, final Context context) {
        //Create a read only aplication session to sign up the user
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {

            @Override
            public void onSuccess(final QBSession session, Bundle params) {
                //User the Auth connection to sign up the user
                QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        //After signin continue to set up call
                        //setUpQB(session, user, context);
                        signIn(context);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        //Unhandled Error
                        error(errors.toString());
                    }
                });
            }

            @Override
            public void onError(List<String> errors) {
                //Unhandled Error
                error(errors.toString());
            }

        });
    }

    /**
     * Shows a dialog with the error string
     * @param s
     */
    private void error(String s) {
        showDialog(s);
    }

    private void setUpQB(final QBSession session, final QBUser user, final Context context) {
        //Set user ID (could be redundant)
        user.setId(session.getUserId());

        // INIT CHAT SERVICE
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }

        //Chat service is used to start WebRTC signaling
        QBChatService.getInstance().login(user, new QBEntityCallbackImpl<QBUser>() {

            @Override
            public void onSuccess() {
                //Set up WebRTC Signaling
                QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                        .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                            @Override
                            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                                if (!createdLocally) {
                                    QBRTCClient.getInstance(context).addSignaling((QBWebRTCSignaling) qbSignaling);
                                }
                            }
                        });

                QBRTCClient.getInstance(context).addSessionCallbacksListener(QuickBloxFragment.this);
                QBRTCConfig.setMaxOpponentsCount(6);
                QBRTCConfig.setDisconnectTime(30);
                QBRTCConfig.setAnswerTimeInterval(30l);
                QBRTCConfig.setDebugEnabled(true);
                //Ready for calls
                QBRTCClient.getInstance(context).prepareToProcessCalls();
                //Let extended classes know that calls are ready
                onQBSetup(session, user);
            }

            @Override
            public void onError(List errors) {
                //error
                error(errors.toString());
            }
        });
    }

    abstract void onQBSetup(QBSession session, QBUser user);

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {
        showDialog("Disconnected!");
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

    }

    @Override
    final public void onReceiveNewSession(final QBRTCSession qbrtcSession) {
        newSessionCreated(qbrtcSession);

        //Answer calls
        final Map<String,String> userInfo = new HashMap<String,String>();
        userInfo.put("Key", "Value");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                qbrtcSession.acceptCall(userInfo);
            }
        });

        //showDialog("New Session Received");
    }

    /**
     * This must be called when a new session is created
     * @param qbrtcSession
     */
    protected void newSessionCreated(QBRTCSession qbrtcSession) {
        //Save the current session
        mCurrentRTCSession = qbrtcSession;
        //Listen for video
        qbrtcSession.addVideoTrackCallbacksListener(QuickBloxFragment.this);
        qbrtcSession.addSessionCallbacksListener(QuickBloxFragment.this);
        qbrtcSession.addSignalingCallback(QuickBloxFragment.this);
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        showDialog("The remote robot did not answer.");
    }

    private void showDialog(final String error) {
        final Activity activity = getActivity();
        if(activity != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Remote Error");
            alertDialog.setMessage(error);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        showDialog("Call Rejected");
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {
        //showDialog("Received Hang Up From User");
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        //showDialog("Session Closed");
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        //showDialog("Call Accepted");
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        //showDialog("Connected to user");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        showDialog(e.toString());
    }
}
