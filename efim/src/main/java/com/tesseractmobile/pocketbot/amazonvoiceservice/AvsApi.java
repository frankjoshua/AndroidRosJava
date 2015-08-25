package com.tesseractmobile.pocketbot.amazonvoiceservice;

import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.OkClient;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import com.squareup.okhttp.OkHttpClient;

public class AvsApi {

    private static AvsApiInterface sAvsApiInterface;
    
    public static AvsApiInterface getAvsApiClient(final String token) {
        if(sAvsApiInterface == null){
            if(token == null){
                throw new UnsupportedOperationException("Token can not be null!");
            }
            
            final RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(LogLevel.FULL)
                .setEndpoint("https://access-alexa-na.amazon.com")
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(new RequestInterceptor() {
                    
                    @Override
                    public void intercept(final RequestFacade request) {
                        request.addHeader("Accept", "application/json");
                        request.addHeader("Authorization", "Bearer " + token);
                    }
                })
                .build();   
            
            sAvsApiInterface = restAdapter.create(AvsApiInterface.class);
        }
        return sAvsApiInterface;
    }
    
    public interface AvsApiInterface {
        @Multipart
        @POST("/v1/avs/speechrecognizer/recognize")
        void getAvsResponse(@Part("request") AvsRequest avsRequest, @Part("audio") TypedFile file, Callback<List<AvsResponse>> callback);
    }
    
//    public static void getAccessToken(Context context, @NotNull String authCode, @NotNull String codeVerifier, AmazonAuthorizationManager authorizationManager, @Nullable final TokenResponseCallback callback){
//        String url = "https://api.amazon.com/auth/O2/token";
//
//        final Map<String, String> arguments = new HashMap<>();
//        arguments.put(ARG_GRANT_TYPE, "authorization_code");
//        arguments.put(ARG_CODE, authCode);
//        try {
//            arguments.put(ARG_REDIRECT_URI, authorizationManager.getRedirectUri());
//            arguments.put(ARG_CLIENT_ID, authorizationManager.getClientId());
//        } catch (AuthError authError) {
//            authError.printStackTrace();
//        }
//        arguments.put(ARG_CODE_VERIFIER, codeVerifier);
//
//        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String s) {
//                Log.i(TAG, s);
//                TokenResponse tokenResponse = new Gson().fromJson(s, TokenResponse.class);
//                REFRESH_TOKEN = tokenResponse.refresh_token;
//                ACCESS_TOKEN = tokenResponse.access_token;
//                if(callback != null){
//                    callback.onSuccess(tokenResponse);
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                volleyError.printStackTrace();
//                if(callback != null){
//                    callback.onFailure(volleyError);
//                }
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return arguments;
//            }
//        };
//        VolleySingleton.getInstance(context).addToRequestQueue(request);
//    }           
}
