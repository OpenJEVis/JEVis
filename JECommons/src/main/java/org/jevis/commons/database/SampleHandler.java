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

import org.jevis.api.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author broder
 */
public class SampleHandler {

    //    private JEVisObject object;
//    public SampleHandler(JEVisObject object) {
//        this.object = object;
//    }
    public SampleHandler() {
    }

    public Period getInputSampleRate(JEVisObject object, String attributeName) {
        Period period = null;
        try {
            period = object.getAttribute(attributeName).getInputSampleRate();
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return period;
    }

    public Boolean getLastSampleAsBoolean(JEVisObject object, String attributeName, boolean defaultValue) {
        boolean lastBoolean = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastBoolean = lastSample.getValueAsBoolean();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastBoolean;
    }

    public <T> T getLastSample(JEVisObject object, String attributeName, T defaultValue) {
        T lastValue = defaultValue;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastValue = getValue(lastSample, defaultValue);
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastValue;
    }

    public String getLastSampleAsString(JEVisObject object, String attributeName) {
        String lastString = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastString = lastSample.getValueAsString();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastString;
    }

    public JEVisFile getLastSampleAsFile(JEVisObject object, String attributeName) {
        JEVisFile lastString = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastString = lastSample.getValueAsFile();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastString;
    }

    private <T> T getValue(JEVisSample lastSample, T defaultValue) {
        T value = defaultValue;
        try {
            if (defaultValue.getClass().equals(String.class)) {
                value = (T) lastSample.getValueAsString();
            } else if (defaultValue.getClass().equals(Boolean.class)) {
                value = (T) lastSample.getValueAsBoolean();
            } else if (defaultValue.getClass().equals(Double.class)) {
                value = (T) lastSample.getValueAsDouble();
            } else if (defaultValue.getClass().equals(Long.class)) {
                value = (T) lastSample.getValueAsLong();
            } else {
                value = (T) lastSample.getValue();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    public DateTime getTimeStampFromLastSample(JEVisObject object, String attributeName) {
        DateTime lastDate = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample smp = attribute.getLatestSample();
                if (smp != null) lastDate = smp.getTimestamp();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastDate;
    }

    public DateTime getTimestampFromFirstSample(JEVisObject object, String attributeName) {
        DateTime firstDate = null;
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                List<JEVisSample> sampleList = attribute.getAllSamples();
                if (sampleList.size() > 0) {
                    JEVisSample smp = sampleList.get(0);
                    firstDate = smp.getTimestamp();
                }
                firstDate = attribute.getTimestampFromFirstSample();

            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return firstDate;
    }

    public List<JEVisSample> getSamplesInPeriod(JEVisObject object, String attributeName, DateTime firstDate, DateTime lastDate) {
        List<JEVisSample> samples = new ArrayList<>();
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                samples = attribute.getSamples(firstDate, lastDate);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return samples;
    }

    public void buildSample(JEVisObject object, String attribute, Object value) {
        try {
            object.getAttribute(attribute).buildSample(new DateTime(), value).commit();
            object.commit();
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<JEVisSample> getAllSamples(JEVisObject object, String attributeName) {
        List<JEVisSample> samples = new ArrayList<>();
        try {
            JEVisAttribute attribute = object.getAttribute(attributeName);
            if (attribute != null) {
                samples = attribute.getAllSamples();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return samples;
    }

    public Object getLastSample(JEVisObject jevisObject, String attributeName) {
        Object lastObject = null;
        try {
            JEVisAttribute attribute = jevisObject.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastObject = lastSample.getValue();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastObject;
    }

    public Long getLastSampleAsLong(JEVisObject jevisObject, String attributeName) {
        Long lastObject = null;
        try {
            JEVisAttribute attribute = jevisObject.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    lastObject = lastSample.getValueAsLong();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastObject;
    }

    public void importData(List<JEVisSample> aggregatedData, JEVisAttribute attribute) {
        try {
            attribute.addSamples(aggregatedData);
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (JEVisException ex) {
            Logger.getLogger(SampleHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
