package com.tesseractmobile.pocketbot.amazonvoiceservice;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by scottab on 07/07/15.
 */
public interface AvsAPIOAuth {

    @Headers("Content-Type: application/json")
    @POST("/O2/token")
    void getAccessToken(@Query("grant_type") String grantType,
            @Query("code") String authCode,
            @Query("redirect_uri") String redirectUri, 
            @Query("client_id") String clientId,
            @Query("code_verifier") String codeVerifier,
            @Body String blank,
            Callback<AccessToken> callback
    );

    @POST("/O2/token")
    void refreshAccessToken(@Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret,
                               @Query("grant_type") String grantType,
                               @Query("refresh_token") String refreshToken,
                               @Body String blank,
                               Callback<AccessToken> callback
    );


}
