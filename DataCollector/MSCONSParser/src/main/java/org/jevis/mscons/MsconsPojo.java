package org.jevis.mscons;

import java.util.ArrayList;
import java.util.List;

public class MsconsPojo {
    private InterchangeHeader interchangeHeader = new InterchangeHeader();
    private MessageHeader messageHeader = new MessageHeader();
    private List<Messlokation> messlokation = new ArrayList<>();


    public InterchangeHeader getInterchangeHeader() {
        return interchangeHeader;
    }

    public void setInterchangeHeader(InterchangeHeader interchangeHeader) {
        this.interchangeHeader = interchangeHeader;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public List<Messlokation> getMesslokation() {
        return messlokation;
    }

    public void setMesslokation(List<Messlokation> messlokation) {
        this.messlokation = messlokation;
    }

    public Messlokation getLastMesslokation() {
        return messlokation.get(messlokation.size() - 1);
    }

    @Override
    public String toString() {
        return "MsconsPojo{" +
                "interchangeHeader=" + interchangeHeader +
                ", messageHeader=" + messageHeader +
                ", messlokation=" + messlokation +
                '}';
    }
}
