package com.tesseractmobile.pocketbot.activities;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.amazonvoiceservice.AvsApi;
import com.tesseractmobile.pocketbot.amazonvoiceservice.AvsApi.AvsApiInterface;
import com.tesseractmobile.pocketbot.amazonvoiceservice.AvsRequest;
import com.tesseractmobile.pocketbot.amazonvoiceservice.AvsResponse;
import com.tesseractmobile.pocketbot.amazonvoiceservice.DeviceContext;
import com.tesseractmobile.pocketbot.amazonvoiceservice.MessageHeader;
import com.tesseractmobile.pocketbot.amazonvoiceservice.Payload;

public class AlexaFaceActivity extends OpenCVFace {
    private static final String        AMAZON_PROFILE_ID    = "amzn1.application.6dcd3bfdc93141d1813ff178cced9734";
    private static final String        AMAZON_CLIENT_ID     = "amzn1.application-oa2-client.cda8a2490ad348f3875212af080b7119";
    private static final String        AMAZON_CLIENT_SECRET = "43c866e90f62e6581db3db2b6817dc46d2b2d43699acc8bec138a9d343cd3ddb";
    protected static final String PRODUCT_ID = "PoketBot";
    protected static final String PRODUCT_DSN = "123456789";
    protected static final String CODE_CHALLENGE = "1";
    protected static final String[] APP_SCOPE = new String[]{"profile"};

    private boolean                    mUseAlexa            = false;
    private String                     mAuthzToken;
    private AmazonAuthorizationManager mAuthManager;
    private ImageButton                mLoginButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (final IllegalArgumentException e) {
            Toast.makeText(this, "Invalid API Key", Toast.LENGTH_LONG).show();
            throw e;
        }

        // Find the button with the login_with_amazon ID
        // and set up a click handler
        mLoginButton = (ImageButton) findViewById(R.id.login_with_amazon);
        mLoginButton.setVisibility(View.VISIBLE);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Bundle options = new Bundle();
                final String scope_data = "{\"alexa:all\":{\"productID\":\"" + PRODUCT_ID +
                                    "\", \"productInstanceAttributes\":{\"deviceSerialNumber\":\"" +
                                    PRODUCT_DSN + "\"}}}";
                options.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, scope_data);
                 
                options.putBoolean(AuthzConstants.BUNDLE_KEY.GET_AUTH_CODE.val, true);
                options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE.val, CODE_CHALLENGE);
                options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE_METHOD.val, "S256");
                mAuthManager.authorize(APP_SCOPE, options, new AuthorizeListener());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthManager.getToken(APP_SCOPE, new TokenListener());
    }

    @Override
    protected void lauchListeningIntent(final String prompt) {
        if (mUseAlexa) {
            // Send Audio to Alexa
            Toast.makeText(this, "Media Player Starting.", Toast.LENGTH_LONG).show();
            final MediaRecorder mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            final File file = new File(getExternalCacheDir(), "test");
            mediaRecorder.setOutputFile(file.getPath());
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            try {
                mediaRecorder.prepare();
            } catch (final IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mediaRecorder.start();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    Toast.makeText(AlexaFaceActivity.this, "Media Player Stopped.", Toast.LENGTH_LONG).show();

                    final MediaPlayer mediaPlayer = MediaPlayer.create(AlexaFaceActivity.this, Uri.parse(file.getPath()));
                    mediaPlayer.start();
                    
                    new AmazonVoiceSerivceTask().execute(file);
                }
            }, 5000);
        } else {
            // Use Google
            super.lauchListeningIntent(prompt);
        }
    }

    private class AuthorizeListener implements AuthorizationListener {

        /* Authorization was completed successfully. */
        @Override
        public void onSuccess(final Bundle response) {
            mAuthManager.getProfile(new ProfileListener());
        }

        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(final AuthError ae) {
            throw new UnsupportedOperationException(ae.toString());
        }

        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(final Bundle cause) {
        }
    }

    private class ProfileListener implements APIListener {

        /* getProfile completed successfully. */
        @Override
        public void onSuccess(final Bundle response) {
            // Retrieve the data we need from the Bundle
            final Bundle profileBundle = response.getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
            final String name = profileBundle.getString(AuthzConstants.PROFILE_KEY.NAME.val);
            final String email = profileBundle.getString(AuthzConstants.PROFILE_KEY.EMAIL.val);
            final String account = profileBundle.getString(AuthzConstants.PROFILE_KEY.USER_ID.val);
            final String zipcode = profileBundle.getString(AuthzConstants.PROFILE_KEY.POSTAL_CODE.val);
            
            // Start using Alexa
            mUseAlexa = true;

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mLoginButton.setVisibility(View.INVISIBLE);
                    Toast.makeText(AlexaFaceActivity.this, "Hello " + name, Toast.LENGTH_LONG).show();
                }
            });

        }

        /* There was an error during the attempt to get the profile. */
        @Override
        public void onError(final AuthError ae) {
        }
    }

    private class TokenListener implements APIListener {

        /* getToken completed successfully. */
        @Override
        public void onSuccess(final Bundle response) {
            //mAuthzToken = response.getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            mAuthzToken = response.getString(AuthzConstants.BUNDLE_KEY.AUTHORIZATION_CODE.val);
            if (!TextUtils.isEmpty(mAuthzToken)) {
                // Retrieve the profile data
                mAuthManager.getProfile(new ProfileListener());

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(AlexaFaceActivity.this, mAuthzToken, Toast.LENGTH_LONG).show();
                    }
                });
            }

        }

        /* There was an error during the attempt to get the token. */
        @Override
        public void onError(final AuthError ae) {
        }
    }

    
    private class AmazonVoiceSerivceTask extends AsyncTask<File, Void, Void>{

        @Override
        protected Void doInBackground(final File... params) {
          

            final AvsApiInterface avsApiClient = AvsApi.getAvsApiClient(mAuthzToken);
            final AvsRequest avsRequest = new AvsRequest();
            final MessageHeader messageHeader = avsRequest.getMessageHeader();
            final DeviceContext deviceContext = new DeviceContext();
            messageHeader.getDeviceContext().add(deviceContext);
            final Payload payload = new Payload();
            payload.setOffsetInMilliseconds("0");
            payload.setStreamId("1");
            payload.setPlayerActivity("IDLE");
            deviceContext.setPayload(payload);
            
            
            avsApiClient.getAvsResponse(avsRequest, new TypedFile("audio/L16; rate=16000; channels=1", params[0]), new Callback<List<AvsResponse>>() {
                
                @Override
                public void success(final List<AvsResponse> arg0, final Response arg1) {
                    say("Connected to amazon");
                }
                
                @Override
                public void failure(final RetrofitError arg0) {
                   say("Could not connect to Amazon error " + arg0);
                }
            });

            return null;
        }
        
    }
}
