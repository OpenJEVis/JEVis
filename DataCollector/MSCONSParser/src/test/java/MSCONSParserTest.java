//import org.jevis.mscons.MsconsParser;
//import org.jevis.mscons.MsconsPojo;
//import org.joda.time.DateTime;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//public class MSCONSParserTest {
//
//    private static MsconsPojo msconsPojo1;
//    private static MsconsPojo msconsPojo2;
// TODO fix timezone problem
//
//    @BeforeAll
//    static void init() {
//        try {
//            InputStream inputStream = new FileInputStream("src/test/resources/MSCONS_TL_9904628000007_9979086000006_20230629_305717319PF.txt");
//            InputStream inputStream2 = new FileInputStream("src/test/resources/TL-example.mscons.txt");
//            MsconsParser msconsParser = new MsconsParser(inputStream);
//            MsconsParser msconsParser2 = new MsconsParser(inputStream2);
//            msconsParser.parse();
//            msconsParser2.parse();
//            msconsPojo1 = msconsParser.getMsconsPojo();
//
//            msconsPojo2 = msconsParser2.getMsconsPojo();
//
//        } catch (FileNotFoundException e) {
//           e.printStackTrace();
//        }
//
//    }
//@Test
//    void checkSender() {
//    assertEquals("20XV-ESCH-AL---R", msconsPojo2.getMessageHeader().getSender());
//    assertEquals("'9904628000007'", msconsPojo1.getMessageHeader().getSender());
//    }
//
//    @Test
//    void checkRecipient() {
//        assertEquals("20XV-ESCH-AL---R", msconsPojo2.getMessageHeader().getRecipient());
//        assertEquals("9979086000006", msconsPojo1.getMessageHeader().getRecipient());
//    }
//
//    @Test
//    void checkCreateDateTime() {
//        DateTime dateTime2 = new DateTime("2019-09-10T09:25:00.000+02:00");
//        DateTime dateTime1 = new DateTime("2023-06-29T07:40:00.000+02:00");
//
//        assertEquals(dateTime1, msconsPojo2.getInterchangeHeader().getCreatedDateTime());
//        assertEquals(dateTime2, msconsPojo2.getInterchangeHeader().getCreatedDateTime());
//    }
//
//    @Test
//    void checkControlReference() {
//        assertEquals("19091000334098", msconsPojo2.getInterchangeHeader().getControlReference());
//        assertEquals("305717319PF", msconsPojo1.getInterchangeHeader().getControlReference());
//    }
//
//    @Test
//    void checkApplicationReference() {
//        assertEquals("TL", msconsPojo2.getInterchangeHeader().getApplicationReference());
//        assertEquals("TL", msconsPojo1.getInterchangeHeader().getApplicationReference());
//    }
//
//    @Test
//    void checkMessageType() {
//        assertEquals("MSCONS", msconsPojo2.getMessageHeader().getMessageType());
//        assertEquals("MSCONS", msconsPojo1.getMessageHeader().getMessageType());
//    }
//
//    @Test
//    void checkMessageVersion() {
//        assertEquals("D", msconsPojo2.getMessageHeader().getMessageVersion());
//        assertEquals("D", msconsPojo1.getMessageHeader().getMessageVersion());
//    }
//
//    @Test
//    void checkMessageRelease() {
//        assertEquals("04B", msconsPojo2.getMessageHeader().getMessageRelease());
//        assertEquals("04B", msconsPojo1.getMessageHeader().getMessageRelease());
//    }
//
//    @Test
//    void checkControllingAgency() {
//        assertEquals("UN", msconsPojo2.getMessageHeader().getControllingAgency());
//        assertEquals("UN", msconsPojo1.getMessageHeader().getControllingAgency());
//    }
//
//    @Test
//    void checkAssociationAssignedCode() {
//        assertEquals("1.0c", msconsPojo2.getMessageHeader().getAssociationAssignedCode());
//        assertEquals("2.4a", msconsPojo1.getMessageHeader().getAssociationAssignedCode());
//    }
//
//    @Test
//    void checkMessageName() {
//        assertEquals("7", msconsPojo2.getMessageHeader().getMessageName());
//        assertEquals("7", msconsPojo1.getMessageHeader().getMessageName());
//    }
//    @Test
//    void checkMessageFunction() {
//        assertEquals("9", msconsPojo2.getMessageHeader().getMessageFunction());
//        assertEquals("9", msconsPojo1.getMessageHeader().getMessageFunction());
//    }
//    @Test
//    void checkMesslokationNumber() {
//        assertEquals("LU00000604360ELEC0000000000022455::89", msconsPojo2.getMesslokation().get(0).getMesslokationNumber());
//        assertEquals("DE0000801335300000000000081324847", msconsPojo1.getMesslokation().get(0).getMesslokationNumber());
//    }
//    @Test
//    void checkMesslokationFrom() {
//        DateTime dateTime2 = new DateTime("2018-10-01T00:00:00.000+02:00");
//        DateTime dateTime1 = new DateTime("2023-06-28T00:00:00.000+02:00");
//        assertEquals(dateTime2, msconsPojo2.getMesslokation().get(0).getFromDateTime());
//        assertEquals(dateTime1, msconsPojo1.getMesslokation().get(0).getFromDateTime());
//    }
//
//    @Test
//    void checkMesslokationUntil() {
//        DateTime dateTime2 = new DateTime("2018-11-01T00:00:00.000+01:00");
//        DateTime dateTime1 = new DateTime("2023-06-29T00:00:00.000+02:00");
//        assertEquals(dateTime2, msconsPojo2.getMesslokation().get(0).getUntilDateTime());
//        assertEquals(dateTime1, msconsPojo1.getMesslokation().get(0).getUntilDateTime());
//    }
//
//    @Test
//    void checkNumberOfSamples() {
//        assertEquals(2980, msconsPojo2.getMesslokation().get(0).getSampleList().size());
//        assertEquals(96, msconsPojo1.getMesslokation().get(0).getSampleList().size());
//    }
//
//    @Test
//    void noMSCONSFile() {
//        try {
//            InputStream inputStream = new FileInputStream("src/test/resources/TL-example.mscons2.txt");
//            MsconsParser msconsParser = new MsconsParser(inputStream);
//            assertEquals(false, msconsParser.parse());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//
//
//}
