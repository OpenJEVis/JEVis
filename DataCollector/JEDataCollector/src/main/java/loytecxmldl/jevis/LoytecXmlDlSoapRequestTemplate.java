package loytecxmldl.jevis;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.jevis.loytecxmldl.jevis.LoytecXmlDlServerClass.NUMBER_OF_SAMPLES_PER_REQUEST;

/**
 *
 */
public class LoytecXmlDlSoapRequestTemplate {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlSoapRequestTemplate.class.getName());

    private String logHandle;
    private String technology;
    private String trendId;

    public LoytecXmlDlSoapRequestTemplate(LoytecXmlDlChannelDirectory channelDirectory, UPCChannel channel) {

        technology = channelDirectory.getTechnology();
        trendId = channel.getTrendId();
    }

    public String getTemplate() {
        return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<SOAP-ENV:Header/>\n" +
                "<SOAP-ENV:Body>\n" +
                "<LogRead xmlns=\"http://www.loytec.com/wsdl/XMLDL/1.0/\" " +
                "NumItems=\"" + NUMBER_OF_SAMPLES_PER_REQUEST + "\" " +
                "ReturnCompleteSet=\"false\" " +
                "StartDateTime=\"${DF:yyyy-MM-dd'T'HH:mm:ss}\">\n" +
                "<ReqBase logHandle=\"" + logHandle + "\"/>\n" +
                "</LogRead>\n" +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }

    public void setLogHandle(String basePath) {
        logHandle = basePath + technology + "/trend-" + trendId + ".bin";
    }
}
