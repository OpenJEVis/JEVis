/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.database;

import junit.framework.Assert;
import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author broder
 */
@RunWith(JUnitParamsRunner.class)
public class JEVisSampleDAOTest {

    JEVisSampleDAO jeVisSampleDAO;
    JEVisObject dataOject = Mockito.mock(JEVisObject.class);
    JEVisAttributeDAO attributeDaoSut = Mockito.mock(JEVisAttributeDAO.class);

    @Before
    public void setup() {
        jeVisSampleDAO = new JEVisSampleDAO(attributeDaoSut);
        JEVisAttribute valueAttribute = mock(JEVisAttribute.class);
        when(valueAttribute.getLatestSample()).thenReturn(mock(JEVisSample.class));
        when(attributeDaoSut.getJEVisAttribute(dataOject, "Value")).thenReturn(valueAttribute);
        JEVisAttribute addressAttribute = mock(JEVisAttribute.class);
        when(addressAttribute.getLatestSample()).thenReturn(mock(JEVisSample.class));
        when(attributeDaoSut.getJEVisAttribute(dataOject, "Address")).thenReturn(addressAttribute);
    }

    private static Object[] getCorrectAttributeNames() {
        return $($("Value"), $("Address"));
    }

    private static Object[] getIncorrectAttributeNames() {
        return $($("Building"), $(""));
    }

    @Test
    @Parameters(method = "getCorrectAttributeNames")
    public void shouldGiveLastValueForAttributeNames(String corretAttributeName) {
        JEVisSample lastJEVisSample = jeVisSampleDAO.getLastJEVisSample(dataOject, corretAttributeName);
        verify(attributeDaoSut).getJEVisAttribute(dataOject, corretAttributeName);
        JEVisAttribute jeVisAttribute = attributeDaoSut.getJEVisAttribute(dataOject, corretAttributeName);
        verify(jeVisAttribute).getLatestSample();
        Assert.assertNotNull(lastJEVisSample);
    }

    @Test
    @Parameters(method = "getIncorrectAttributeNames")
    public void shouldGiveNullForInvalidAttributeNames(String corretAttributeName) {
        JEVisSample lastJEVisSample = jeVisSampleDAO.getLastJEVisSample(dataOject, corretAttributeName);
        verify(attributeDaoSut).getJEVisAttribute(dataOject, corretAttributeName);
        Assert.assertNull(lastJEVisSample);
    }

    @Test
    @Parameters(method = "getCorrectAttributeNames")
    public void shouldGiveAllValuesInPeriodForAttributeNames(String corretAttributeName) {
        jeVisSampleDAO.getSamplesInPeriod(dataOject, corretAttributeName, any(DateTime.class), any(DateTime.class));
        verify(attributeDaoSut).getJEVisAttribute(dataOject, corretAttributeName);
        JEVisAttribute jeVisAttribute = attributeDaoSut.getJEVisAttribute(dataOject, corretAttributeName);
        verify(jeVisAttribute).getSamples(any(DateTime.class), any(DateTime.class));
    }
}
