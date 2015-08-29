package com.tesseractmobile.pocketbot.amazonvoiceservice;

import com.google.gson.annotations.Expose;

public class Payload {

    @Expose
    private String streamId;
    @Expose
    private String offsetInMilliseconds;
    @Expose
    private String playerActivity;

    /**
     * 
     * @return The streamId
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * 
     * @param streamId
     *            The streamId
     */
    public void setStreamId(final String streamId) {
        this.streamId = streamId;
    }

    /**
     * 
     * @return The offsetInMilliseconds
     */
    public String getOffsetInMilliseconds() {
        return offsetInMilliseconds;
    }

    /**
     * 
     * @param offsetInMilliseconds
     *            The offsetInMilliseconds
     */
    public void setOffsetInMilliseconds(final String offsetInMilliseconds) {
        this.offsetInMilliseconds = offsetInMilliseconds;
    }

    /**
     * 
     * @return The playerActivity
     */
    public String getPlayerActivity() {
        return playerActivity;
    }

    /**
     * 
     * @param playerActivity
     *            The playerActivity
     */
    public void setPlayerActivity(final String playerActivity) {
        this.playerActivity = playerActivity;
    }

}