package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.List;

/**
 * Created by josh on 11/1/2015.
 */
public class SignInFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    private EditText mEmail;
    private EditText mPassword;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.sign_in_layout, null);
        view.findViewById(R.id.btnSignIn).setOnClickListener(this);
        mEmail = (EditText) view.findViewById(R.id.etEmail);
        mPassword = (EditText) view.findViewById(R.id.etPassword);
        mEmail.setText(PocketBotSettings.getUserName(getActivity()));

        return new AlertDialog.Builder(getActivity())
                .setTitle("Sign In for Telepresence")
                .setView(view)
                .create();
    }

    @Override
    public void onClick(View view) {
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        if(TextUtils.isEmpty(password) || password.length() < 8)
        {
            mPassword.setError("You must have at least 8 characters in your password");
            return;
        }

        final QBUser qbUser = new QBUser(email, password);

        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>(){
            @Override
            public void onError(List<String> errors) {
                if(errors.get(0).equals("Unauthorized")){
                    signUpUser(qbUser);
                } else {
                    mEmail.setError(errors.get(0));;
                }
            }

            @Override
            public void onSuccess(QBSession result, Bundle params) {
                onSuccessfulSignIn();
            }
        });
    }

    private void onSuccessfulSignIn() {
        //Save user name and password
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        PocketBotSettings.setUserName(getActivity(), email);
        PocketBotSettings.setPassword(getActivity(), password);
        //Mark user as signed in
        PocketBotSettings.setSignedIn(getActivity(), true);
        //Close fragment
        getActivity().getSupportFragmentManager().beginTransaction().remove(SignInFragment.this).commit();
    }

    private void signUpUser(final QBUser qbUser) {
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>(){

            @Override
            public void onError(List<String> errors) {
                mEmail.setError(errors.get(0));;
            }

            @Override
            public void onSuccess(QBSession result, Bundle params) {
                QBUsers.signUp(qbUser, new QBEntityCallbackImpl<QBUser>(){
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        onSuccessfulSignIn();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        mEmail.setError(errors.get(0));
                    }
                });
            }
        });
    }
}
