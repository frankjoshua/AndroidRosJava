package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.List;
import java.util.Map;

/**
 * Created by josh on 11/1/2015.
 */
abstract public class QuickBloxFragment extends FaceFragment implements QBRTCClientSessionCallbacks, QBRTCClientConnectionCallbacks, QBRTCClientVideoTracksCallbacks {

    private QBChatService chatService;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        //Check if signed in
        if(PocketBotSettings.isSignedIn(activity)){

        } else {

        }

        //Sign in user
        signIn(activity);
    }

    protected void signIn(final Activity activity) {
        final String login = PocketBotSettings.getRobotId(activity);
        final String password = PocketBotSettings.getPassword(activity);

        if(TextUtils.isEmpty(login) || TextUtils.isEmpty(password)){
            //Launch sign in fragment
            FragmentTransaction fragmentTransaction = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
            new SignInDialog().show(fragmentTransaction, "SIGN_IN_FRAGMENT");
            return;
        }

        final QBUser user = new QBUser(login, password);
        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                PocketBotSettings.setQuickBloxId(getActivity(), session.getUserId());
                setUpQB(session, user, activity);
            }

            @Override
            public void onError(List<String> errors) {
                //error
                if (errors.get(0).equals("Unauthorized")) {
                    signUpUser(user);
                } else {
                    error(errors.toString());
                }
            }
        });
    }

    private void signUpUser(final QBUser user) {
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>(){

            @Override
            public void onError(List<String> errors) {
                error(errors.toString());
            }

            @Override
            public void onSuccess(final QBSession session, Bundle params) {
                QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        setUpQB(session, user, getActivity());
                    }

                    @Override
                    public void onError(List<String> errors) {
                        error(errors.toString());;
                    }
                });
            }
        });
    }

    private void error(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    private void setUpQB(final QBSession session, final QBUser user, final Activity activity) {
        user.setId(session.getUserId());

        // INIT CHAT SERVICE
        if (!QBChatService.isInitialized()) {
            QBChatService.init(activity);
        }

        // LOG IN CHAT SERVICE
        chatService = QBChatService.getInstance();
        chatService.login(user, new QBEntityCallbackImpl<QBUser>() {

            @Override
            public void onSuccess() {
                // success
                QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                        .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                            @Override
                            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                                if (!createdLocally) {
                                    QBRTCClient.getInstance().addSignaling((QBWebRTCSignaling) qbSignaling);
                                }
                            }
                        });

                QBRTCClient.getInstance().addSessionCallbacksListener(QuickBloxFragment.this);
                QBRTCClient.getInstance().addVideoTrackCallbacksListener(QuickBloxFragment.this);
                QBRTCClient.getInstance().addConnectionCallbacksListener(QuickBloxFragment.this);

                QBRTCClient.getInstance().prepareToProcessCalls(activity);
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
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {

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
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        final Activity activity = getActivity();
        if(activity != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Remote Error");
            alertDialog.setMessage("The remote robot did not answer.");
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

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }
}
