package com.tesseractmobile.pocketbot.amazonvoiceservice;

import com.google.gson.annotations.Expose;

public class DeviceContext {

    @Expose
    final private String name = "playbackState";
    @Expose
    final private String  namespace = "AudioPlayer";
    @Expose
    private Payload payload;

    /**
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }


    /**
     * 
     * @return The namespace
     */
    public String getNamespace() {
        return namespace;
    }


    /**
     * 
     * @return The payload
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * 
     * @param payload
     *            The payload
     */
    public void setPayload(final Payload payload) {
        this.payload = payload;
    }

}