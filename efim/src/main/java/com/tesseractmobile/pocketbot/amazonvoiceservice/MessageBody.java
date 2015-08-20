package com.tesseractmobile.pocketbot.amazonvoiceservice;

import com.google.gson.annotations.Expose;

public class MessageBody {

    @Expose
    final private String profile = "doppler-scone";
    @Expose
    final private String locale = "en-us";
    @Expose
    final private String format = "audio/L16; rate=16000; channels=1";

    /**
     * 
     * @return The profile
     */
    public String getProfile() {
        return profile;
    }
    
    /**
     * 
     * @return The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * 
     * @return The format
     */
    public String getFormat() {
        return format;
    }

}