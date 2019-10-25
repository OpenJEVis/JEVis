package org.jevis.jenotifier.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.utils.PrettyError;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.TreeMap;

public class CSVExportLink extends ExportLink {
    private static final Logger logger = LogManager.getLogger(CSVExportLink.class);
    public static String CLASS_NAME = "CSV Export Link";
    public static String TYPE_COLUMN = "Column ID";
    public static String TYPE_VALUE_FORMAT = "Value Format";


    protected JEVisAttribute attColumn;
    protected JEVisAttribute attValueFormat;

    protected String valueFormate = "f";
    private DecimalFormat decimalFormat;
    protected int column = -1;


    public CSVExportLink(JEVisObject object) {
        super(object);
        super.init();
    }

    @Override
    void initAttributes() {
        try {
            attColumn = linkObject.getAttribute(TYPE_COLUMN);
            column = attColumn.getLatestSample().getValueAsLong().intValue();
        } catch (Exception ex) {
//            e.printStackTrace();
            logger.error("Error in Column ID: {}", PrettyError.getJEVisLineFilter(ex));
        }


        try {
            decimalFormat = new DecimalFormat("##.####################");
            attValueFormat = linkObject.getAttribute(TYPE_VALUE_FORMAT);
            valueFormate = attValueFormat.getLatestSample().getValueAsString();

//            System.out.println("---valueFormate: " + valueFormate);

            if (valueFormate != null || !valueFormate.isEmpty()) {
                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
                otherSymbols.setDecimalSeparator(valueFormate.charAt(0));
//                System.out.println("---otherSymbols: " + otherSymbols.getDecimalSeparator());
                decimalFormat.setDecimalFormatSymbols(otherSymbols);
                decimalFormat.setGroupingUsed(false);
//                System.out.println("---decimalFormat: " + decimalFormat.getDecimalFormatSymbols());
//                System.out.println("---Decimal test: " + decimalFormat.format(12313.23));
                //TODO case String formate
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("[{}] Error in Decimal Format: {}", linkObject.getID(), PrettyError.getJEVisLineFilter(e));
        }

    }


    @Override
    Map<DateTime, JEVisSample> getSamples(DateTime from, DateTime until) {
        Map<DateTime, JEVisSample> dateTimeJEVisSampleMap = new TreeMap<>();
        if (getTargetAttribute() != null) {
            for (JEVisSample sample : getTargetAttribute().getSamples(from, until)) {
                try {
                    dateTimeJEVisSampleMap.put(sample.getTimestamp(), sample);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return dateTimeJEVisSampleMap;
    }

    @Override
    String formatValue(Object sample) {
        if (sample instanceof Double) {
            //System.out.println("Using double format: " + decimalFormat.format(sample));
            return decimalFormat.format(sample);
        }
//        else if (sample instanceof String) {
//            return String.format(valueFormate, sample);
//        }
        //System.out.println("using to string value");
        return sample.toString();

//        System.out.print("Value type: " + sample);
//        System.out.println("v2 " + String.format("%s", sample));
//        return String.format("%s", sample);
//        if (sample instanceof Double) {
//            String.format(valueFormate, sample);
//        }

//        return "";
    }

    @Override
    int getColumn() {
        return column;
    }
}
