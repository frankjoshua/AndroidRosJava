package com.tesseractmobile.pocketbot.amazonvoiceservice;

import com.google.gson.annotations.Expose;

public class AvsRequest {

    @Expose
    private MessageHeader messageHeader = new MessageHeader();
    @Expose
    private MessageBody   messageBody = new MessageBody();

    
    
    /**
     * 
     * @return The messageHeader
     */
    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    /**
     * 
     * @param messageHeader
     *            The messageHeader
     */
    public void setMessageHeader(final MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    /**
     * 
     * @return The messageBody
     */
    public MessageBody getMessageBody() {
        return messageBody;
    }

    /**
     * 
     * @param messageBody
     *            The messageBody
     */
    public void setMessageBody(final MessageBody messageBody) {
        this.messageBody = messageBody;
    }

}