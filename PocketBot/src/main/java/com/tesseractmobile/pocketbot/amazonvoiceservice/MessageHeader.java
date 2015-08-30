package com.tesseractmobile.pocketbot.amazonvoiceservice;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class MessageHeader {

    @Expose
    private List<DeviceContext> deviceContext = new ArrayList<DeviceContext>();

    /**
     * 
     * @return The deviceContext
     */
    public List<DeviceContext> getDeviceContext() {
        return deviceContext;
    }

    /**
     * 
     * @param deviceContext
     *            The deviceContext
     */
    public void setDeviceContext(final List<DeviceContext> deviceContext) {
        this.deviceContext = deviceContext;
    }

}