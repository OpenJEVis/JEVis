package org.jevis.commons.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.util.CellAddress;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.joda.time.DateTime;

public class ReportLink {
    private static final Logger logger = LogManager.getLogger(ReportLink.class);
    private String name;
    private Long JEVisId;
    private boolean optional;
    private String templateVariableName;

    private String sheet;
    private CellAddress cellAddress;


    private Status linkStatus = Status.NEW;

    private JEVisObject jeVisObject;

    private ReportAttribute reportAttribute;

    public ReportLink(String name, Long JEVisId, boolean optional, String templateVariableName, ReportAttribute reportAttribute) {
        this.name = name;
        this.JEVisId = JEVisId;
        this.optional = optional;
        this.templateVariableName = templateVariableName;
        this.reportAttribute = reportAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getJEVisId() {
        return JEVisId;
    }

    public void setJEVisId(Long JEVisId) {
        this.JEVisId = JEVisId;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getTemplateVariableName() {
        return templateVariableName;
    }

    public void setTemplateVariableName(String templateVariableName) {
        this.templateVariableName = templateVariableName;
    }


    public ReportAttribute getReportAttribute() {
        return reportAttribute;
    }

    public void setReportAttribute(ReportAttribute reportAttribute) {
        this.reportAttribute = reportAttribute;
    }

    @Override
    public ReportLink clone() {
        ReportLink clonedReportLink = new ReportLink(
                this.getName(), this.getJEVisId(), this.isOptional(), this.getTemplateVariableName(),
                new ReportAttribute(getReportAttribute().getAttributeName(),
                        new ReportPeriodConfiguration(getReportAttribute().getReportPeriodConfiguration().getReportAggregation(),
                                getReportAttribute().getReportPeriodConfiguration().getReportManipulation(),
                                getReportAttribute().getReportPeriodConfiguration().getPeriodMode(),
                                getReportAttribute().getReportPeriodConfiguration().getFixedPeriod())));

        clonedReportLink.setLinkStatus(this.getLinkStatus());

        return clonedReportLink;
    }

    @Override
    public String toString() {
        return "ReportLink{" +
                "name='" + name + '\'' +
                ", jEVisID=" + JEVisId +
                ", optional=" + optional +
                ", templateVariableName='" + templateVariableName + '\'' +
                ", reportAttribute=" + reportAttribute +
                '}';
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public CellAddress getCellAddress() {
        return cellAddress;
    }

    public void setCellAddress(CellAddress cellAddress) {
        this.cellAddress = cellAddress;
    }

    public JEVisObject getJeVisObject() {
        return jeVisObject;
    }

    public void setJeVisObject(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public Status getLinkStatus() {
        return linkStatus;
    }

    public void setLinkStatus(Status linkStatus) {
        this.linkStatus = linkStatus;
    }

    public static enum Status {
        NEW,
        UPDATE,
        DELETE,
        FALSE
    }

    public void update() throws JEVisException {
        logger.debug("Update Report Link: ", this);

        DateTime dateTime = new DateTime();
        JEVisObject jevisObjectReportAttribute = null;
        JEVisObject jevisObjectReportPeriodeConfiguration = null;


        if (jeVisObject.getChildren().size() > 0) {

            if (jeVisObject.getChildren().get(0).getJEVisClassName().equals(JC.ReportAttribute.name)) {
                jevisObjectReportAttribute = jeVisObject.getChildren().get(0);
                if (jevisObjectReportAttribute.getChildren().size() > 0) {
                    if (jevisObjectReportAttribute.getChildren().get(0).getJEVisClassName().equals(JC.ReportConfiguration.ReportPeriodConfiguration.name)) {
                        jevisObjectReportPeriodeConfiguration = jevisObjectReportAttribute.getChildren().get(0);
                    }
                }

            }
        }

        if (jeVisObject == null) return;
        updateJevisID(dateTime);
        updateOptional(dateTime);
        if (reportAttribute == null) return;
        if (reportAttribute.getReportPeriodConfiguration() == null) return;
        updateAggregation(dateTime, jevisObjectReportPeriodeConfiguration);
        updateManipulation(dateTime, jevisObjectReportPeriodeConfiguration);
        updatePeriod(dateTime, jevisObjectReportPeriodeConfiguration);
        updateFixedPeriod(dateTime, jevisObjectReportPeriodeConfiguration);


    }

    private void updateFixedPeriod(DateTime dateTime, JEVisObject jevisObjectReportAttribute) throws JEVisException {
        if (reportAttribute.getReportPeriodConfiguration().getReportManipulation() == null) return;
        JEVisAttribute jeVisAttribute = jevisObjectReportAttribute.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_FixedPeriod);
        if (jeVisAttribute == null) return;
        if (jeVisAttribute.hasSample()) {
            if (!jeVisAttribute.getLatestSample().getValueAsString().equals(reportAttribute.getReportPeriodConfiguration().getFixedPeriod().toString())) {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getFixedPeriod().toString());
                sample.commit();
            }
        } else {
            JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getFixedPeriod().toString());
            sample.commit();
        }
    }

    private void updatePeriod(DateTime dateTime, JEVisObject jevisObjectReportAttribute) throws JEVisException {
        if (reportAttribute.getReportPeriodConfiguration().getReportManipulation() == null) return;
        JEVisAttribute jeVisAttribute = jevisObjectReportAttribute.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Period);
        if (jeVisAttribute == null) return;
        if (jeVisAttribute.hasSample()) {
            if (!jeVisAttribute.getLatestSample().getValueAsString().equals(reportAttribute.getReportPeriodConfiguration().getPeriodMode().toString())) {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getPeriodMode().toString());
                sample.commit();
            }
        } else {
            JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getPeriodMode().toString());
            sample.commit();
        }
    }

    private void updateManipulation(DateTime dateTime, JEVisObject jevisObjectReportAttribute) throws JEVisException {
        if (reportAttribute.getReportPeriodConfiguration().getReportManipulation() == null) return;
        JEVisAttribute jeVisAttribute = jevisObjectReportAttribute.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Manipulation);
        if (jeVisAttribute == null) return;
        if (jeVisAttribute.hasSample()) {
            if (!jeVisAttribute.getLatestSample().getValueAsString().equals(reportAttribute.getReportPeriodConfiguration().getReportManipulation().toString())) {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getReportManipulation().toString());
                sample.commit();
            }
        } else {
            JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getReportManipulation().toString());
            sample.commit();
        }


    }

    private void updateAggregation(DateTime dateTime, JEVisObject jevisObjectReportAttribute) throws JEVisException {
        if (reportAttribute.getReportPeriodConfiguration().getReportAggregation() == null) return;
        JEVisAttribute jeVisAttribute = jevisObjectReportAttribute.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Aggregation);
        if (jeVisAttribute == null) return;
        if (jeVisAttribute.hasSample()) {
            if (!jeVisAttribute.getLatestSample().getValueAsString().equals(reportAttribute.getReportPeriodConfiguration().getReportAggregation().toString())) {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getReportAggregation().toString());

                sample.commit();
            }
        } else {
            JEVisSample sample = jeVisAttribute.buildSample(dateTime, reportAttribute.getReportPeriodConfiguration().getReportAggregation().toString());
            sample.commit();
        }
    }

    private void updateOptional(DateTime dateTime) throws JEVisException {
        JEVisAttribute jeVisAttribute = jeVisObject.getAttribute(JC.ReportLink.a_Optional);
        if (jeVisAttribute == null) return;
        if (jeVisAttribute.hasSample()) {
            if (!jeVisAttribute.getLatestSample().getValueAsBoolean().equals(optional)) {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, optional);
                sample.commit();
            }
        } else {
            JEVisSample sample = jeVisAttribute.buildSample(dateTime, optional);
            sample.commit();
        }

    }

    private void updateJevisID(DateTime dateTime) throws JEVisException {
        if (JEVisId != 0) {
            JEVisAttribute jeVisAttribute = jeVisObject.getAttribute(JC.ReportLink.a_JEVisID);
            if (jeVisAttribute == null) return;
            if (jeVisAttribute.hasSample()) {
                if (jeVisAttribute.getLatestSample().getValueAsLong() != JEVisId) {
                    JEVisSample sample = jeVisAttribute.buildSample(dateTime, JEVisId);
                    sample.commit();
                }
            } else {
                JEVisSample sample = jeVisAttribute.buildSample(dateTime, JEVisId);
                sample.commit();
            }
        }


    }

    public static ReportLink parseFromJEVisObject(JEVisObject jeVisObject) throws RuntimeException {
        logger.debug("parse JEVis Obejct: ", jeVisObject);
        if (jeVisObject != null) {
            String name = null;
            Long jevisID = null;
            boolean optional = false;
            String variableTemplateName = null;
            String attributeName = null;
            AggregationPeriod aggregationPeriod1 = null;
            ManipulationMode manipulationMode = null;
            PeriodMode periodMode = null;
            FixedPeriod fixedPeriod1 = null;
            try {
                JEVisObject reportAttribute = null;
                JEVisObject reportPeriodeConfiguration = null;


                name = null;
                jevisID = null;
                optional = false;
                variableTemplateName = null;

                name = jeVisObject.getName();
                if (jeVisObject.getAttribute(JC.ReportLink.a_JEVisID).hasSample()) {
                    jevisID = jeVisObject.getAttribute(JC.ReportLink.a_JEVisID).getLatestSample().getValueAsLong();
                }
                if (jeVisObject.getAttribute(JC.ReportLink.a_Optional).hasSample()) {
                    optional = jeVisObject.getAttribute(JC.ReportLink.a_Optional).getLatestSample().getValueAsBoolean();
                }
                if (jeVisObject.getAttribute(JC.ReportLink.a_TemplateVariableName).hasSample()) {
                    variableTemplateName = jeVisObject.getAttribute(JC.ReportLink.a_TemplateVariableName).getLatestSample().getValueAsString();
                }


                attributeName = null;

                if (jeVisObject.getChildren() != null && jeVisObject.getChildren().size() > 0) {
                    reportAttribute = jeVisObject.getChildren().get(0);
                    if (reportAttribute.getAttribute(JC.ReportAttribute.a_AttributeName).hasSample()) {
                        attributeName = reportAttribute.getAttribute(JC.ReportAttribute.a_AttributeName).getLatestSample().getValueAsString();
                    }
                }


                String aggregationPeriod = null;
                String manipulation = null;
                String periode = null;
                String fixedPeriod = null;

                if (reportAttribute != null) {
                    if (reportAttribute.getChildren().size() > 0) {


                        reportPeriodeConfiguration = reportAttribute.getChildren().get(0);
                        if (reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Aggregation).hasSample()) {
                            aggregationPeriod = reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Aggregation).getLatestSample().getValueAsString();
                        }

                        if (reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Manipulation).hasSample()) {
                            manipulation = reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Manipulation).getLatestSample().getValueAsString();

                        }
                        if (reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Period).hasSample()) {
                            periode = reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Period).getLatestSample().getValueAsString();
                        }
                        if (reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_FixedPeriod).hasSample()) {
                            fixedPeriod = reportPeriodeConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_FixedPeriod).getLatestSample().getValueAsString();
                        }
                        aggregationPeriod1 = AggregationPeriod.parseAggregation(aggregationPeriod);
                        manipulationMode = ManipulationMode.parseManipulation(manipulation);
                        periodMode = PeriodMode.valueOf(periode);
                        fixedPeriod1 = FixedPeriod.parseFixedPeriod(fixedPeriod);

                    }
                }



            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }


            ReportLink reportLink = new ReportLink(name, jevisID, optional, variableTemplateName, new ReportAttribute(attributeName, new ReportPeriodConfiguration(aggregationPeriod1, manipulationMode, periodMode, fixedPeriod1)));
            reportLink.setJeVisObject(jeVisObject);
            return reportLink;
        }
        return null;

    }
}

