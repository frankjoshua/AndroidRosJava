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
}
