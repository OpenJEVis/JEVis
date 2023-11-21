package org.jevis.mscons;

public class MessageHeader {

    private String messageIdentifier;

    private String messageType;

    private String messageVersion;

    private String messageRelease;

    private String controllingAgency;

    private String associationAssignedCode;

    private String messageName;
    private String messageIdentification;
    private String messageFunction;

    private String messageDtmQualifier;
    private String messageDtm;
    private String messageDtmFormat;

    private String sender;
    private String recipient;
    private String deliveryParty;


    public String getMessageIdentifier() {
        return messageIdentifier;
    }

    public void setMessageIdentifier(String messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageVersion() {
        return messageVersion;
    }

    public void setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
    }

    public String getMessageRelease() {
        return messageRelease;
    }

    public void setMessageRelease(String messageRelease) {
        this.messageRelease = messageRelease;
    }

    public String getControllingAgency() {
        return controllingAgency;
    }

    public void setControllingAgency(String controllingAgency) {
        this.controllingAgency = controllingAgency;
    }

    public String getAssociationAssignedCode() {
        return associationAssignedCode;
    }

    public void setAssociationAssignedCode(String associationAssignedCode) {
        this.associationAssignedCode = associationAssignedCode;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getMessageIdentification() {
        return messageIdentification;
    }

    public void setMessageIdentification(String messageIdentification) {
        this.messageIdentification = messageIdentification;
    }

    public String getMessageFunction() {
        return messageFunction;
    }

    public void setMessageFunction(String messageFunction) {
        this.messageFunction = messageFunction;
    }

    public String getMessageDtmQualifier() {
        return messageDtmQualifier;
    }

    public void setMessageDtmQualifier(String messageDtmQualifier) {
        this.messageDtmQualifier = messageDtmQualifier;
    }

    public String getMessageDtm() {
        return messageDtm;
    }

    public void setMessageDtm(String messageDtm) {
        this.messageDtm = messageDtm;
    }

    public String getMessageDtmFormat() {
        return messageDtmFormat;
    }

    public void setMessageDtmFormat(String messageDtmFormat) {
        this.messageDtmFormat = messageDtmFormat;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getDeliveryParty() {
        return deliveryParty;
    }

    public void setDeliveryParty(String deliveryParty) {
        this.deliveryParty = deliveryParty;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "messageIdentifier='" + messageIdentifier + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageVersion='" + messageVersion + '\'' +
                ", messageRelease='" + messageRelease + '\'' +
                ", controllingAgency='" + controllingAgency + '\'' +
                ", associationAssignedCode='" + associationAssignedCode + '\'' +
                ", messageName='" + messageName + '\'' +
                ", messageIdentification='" + messageIdentification + '\'' +
                ", messageFunction='" + messageFunction + '\'' +
                ", messageDtmQualifier='" + messageDtmQualifier + '\'' +
                ", messageDtm='" + messageDtm + '\'' +
                ", messageDtmFormat='" + messageDtmFormat + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + recipient + '\'' +
                ", deliveryParty='" + deliveryParty + '\'' +
                '}';
    }


}
