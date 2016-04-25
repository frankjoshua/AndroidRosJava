package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.nearby.Nearby;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.io.IOException;

/**
 * Created by josh on 3/27/2016.
 */
public class GoogleSignInController implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String TAG = GoogleSignInController.class.getSimpleName();

    private final GoogleSignInOptions gso;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;


    public GoogleSignInController(final FragmentActivity fragmentActivity) {
       gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                //.enableAutoManage(fragmentActivity, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    public void startSignin(final FragmentActivity fragmentActivity, final int requestCode) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        fragmentActivity.startActivityForResult(signInIntent, requestCode);
    }

    public void handleSignInResult(final Context context, final GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            getGoogleOAuthTokenAndLogin(context, acct.getEmail());
        } else {
            // Signed out, show unauthenticated UI.
            throw new UnsupportedOperationException();
        }
    }

    private void getGoogleOAuthTokenAndLogin(final Context context, final String account) {
        /* Get OAuth token in Background */
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(String... params) {
                String token = null;

                try {
                    //String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    Log.d(TAG, "Trying to connect to Google API");
                    final String scope = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(context, params[0], scope);
                    //token = GoogleAuthUtil.getToken(BaseFaceFragmentActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                    Log.d(TAG, "Token read: " + token);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                if (token != null) {
                    onTokenReceived(context, token);
                } else if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        };
        task.execute(account);
    }

    private Context mContext;
    private String mToken;
    private void onTokenReceived(final Context context, final String token){
        mContext = context;
        mToken = token;
        Log.d(TAG, "Received token: " + token);

        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onConnected(Bundle result) {
        Robot.get().setAuthToken(PocketBotSettings.getRobotId(mContext), mToken);
        Toast.makeText(mContext, "Google Sign-In Complete", Toast.LENGTH_LONG).show();
        //mSignInButton.setEnabled(true);
        //Auto sign in next time
        PocketBotSettings.setAutoSignIn(mContext, true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
        throw new UnsupportedOperationException();
    }

    public Scope[] getScopeArray() {
        return gso.getScopeArray();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
