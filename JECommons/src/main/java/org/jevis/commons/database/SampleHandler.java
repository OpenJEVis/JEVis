/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.database;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class SampleHandler {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SampleHandler.class);

    //    private JEVisObject object;
//    public SampleHandler(JEVisObject object) {
//        this.object = object;
//    }
    public SampleHandler() {
    }

    public Boolean getLastSample(JEVisObject object, String attributeName, boolean defaultValue) {
        boolean lastBoolean = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastBoolean = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastBoolean;
    }

    public Double getLastSample(JEVisObject object, String attributeName, Double defaultValue) {
        Double lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public String getLastSample(JEVisObject object, String attributeName, String defaultValue) {
        String lastString = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastString = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastString;
    }

    public AggregationPeriod getLastSample(JEVisObject object, String attributeName, AggregationPeriod defaultValue) {
        AggregationPeriod lastAggregationPeriod = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastAggregationPeriod = AggregationPeriod.parseAggregation(lastSample.getValueAsString());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastAggregationPeriod;
    }

    public ManipulationMode getLastSample(JEVisObject object, String attributeName, ManipulationMode defaultValue) {
        ManipulationMode lastManipulationMode = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastManipulationMode = ManipulationMode.parseManipulation(lastSample.getValueAsString());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastManipulationMode;
    }

    public Long getLastSample(JEVisObject object, String attributeName, Long defaultValue) {
        Long lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public Object getLastSample(JEVisObject object, String attributeName, Object defaultValue) {
        Object lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public Period getLastSample(JEVisObject object, String attributeName, Period defaultValue) {
        Period lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = new Period(lastSample.getValueAsString());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public DateTime getLastSample(JEVisObject object, String attributeName, DateTime defaultValue) {
        DateTime lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = new DateTime(lastSample.getValueAsString());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public DateTimeZone getLastSample(JEVisObject object, String attributeName, DateTimeZone defaultValue) {
        DateTimeZone lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = DateTimeZone.forID(lastSample.getValueAsString());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    public JEVisFile getLastSample(JEVisObject object, String attributeName, JEVisFile defaultValue) {
        JEVisFile lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = getValue(lastSample, defaultValue);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastValue;
    }

    private String getValue(JEVisSample lastSample, String defaultValue) {
        try {
            return lastSample.getValueAsString();
        } catch (Exception e) {
            logger.error("Error when getting String value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    private Boolean getValue(JEVisSample lastSample, Boolean defaultValue) {
        try {
            return lastSample.getValueAsBoolean();
        } catch (Exception e) {
            logger.error("Error when getting Boolean value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    private Double getValue(JEVisSample lastSample, Double defaultValue) {
        try {
            return lastSample.getValueAsDouble();
        } catch (Exception e) {
            logger.error("Error when getting Double value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    private Long getValue(JEVisSample lastSample, Long defaultValue) {
        try {
            return lastSample.getValueAsLong();
        } catch (Exception e) {
            logger.error("Error when getting Long value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    private Object getValue(JEVisSample lastSample, Object defaultValue) {
        try {
            return lastSample.getValue();
        } catch (Exception e) {
            logger.error("Error when getting Object value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    private JEVisFile getValue(JEVisSample lastSample, JEVisFile defaultValue) {
        try {
            return lastSample.getValueAsFile();
        } catch (Exception e) {
            logger.error("Error when getting JEVisFile value from sample {}.", lastSample, e);
        }
        return defaultValue;
    }

    public DateTime getTimeStampOfLastSample(JEVisObject object, String attributeName) {
        DateTime lastDate = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample smp = attribute.getLatestSample();
                if (smp != null) lastDate = smp.getTimestamp();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastDate;
    }

    public DateTime getTimestampOfFirstSample(JEVisObject object, String attributeName) {
        DateTime firstDate = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null && attribute.hasSample()) {
                firstDate = attribute.getTimestampOfFirstSample();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return firstDate;
    }

    public JEVisSample getFirstSample(JEVisObject object, String attributeName) {
        JEVisSample firstSample = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                List<JEVisSample> sampleList = attribute.getAllSamples();
                if (sampleList.size() > 0) {
                    firstSample = sampleList.get(0);
                }

            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return firstSample;
    }

    public List<JEVisSample> getSamplesInPeriod(JEVisObject object, String attributeName, DateTime firstDate, DateTime lastDate) {
        List<JEVisSample> samples = new ArrayList<>();
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                samples = attribute.getSamples(firstDate, lastDate);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return samples;
    }

    public void buildSample(JEVisObject object, String attribute, Object value) {
        try {
            object.getAttribute(attribute).buildSample(new DateTime(), value).commit();
            object.commit();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public List<JEVisSample> getAllSamples(JEVisObject object, String attributeName) {
        List<JEVisSample> samples = new ArrayList<>();
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                samples = attribute.getAllSamples();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return samples;
    }

    public void importData(List<JEVisSample> aggregatedData, JEVisAttribute attribute) {
        try {
            attribute.addSamples(aggregatedData);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void importDataAndReplaceSorted(List<JEVisSample> aggregatedData, JEVisAttribute attribute) {
        if (aggregatedData.isEmpty()) {
            return;
        }
        try {
            DateTime from = aggregatedData.get(0).getTimestamp();
            DateTime to = aggregatedData.get(aggregatedData.size() - 1).getTimestamp();
            attribute.deleteSamplesBetween(from, to);
            attribute.addSamples(aggregatedData);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
