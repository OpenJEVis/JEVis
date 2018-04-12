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

import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author broder
 */
@RunWith(JUnitParamsRunner.class)
public class JEVisAttributeDAOTest {

    private JEVisAttributeDAO attributeDao;
    private JEVisObject dataObject;

    private static Object[] getCorrectAttributeNames() {
        return $($("Value"), $("Building"), $("Members"));
    }

    private static Object[] getIncorrectAttributeNames() {
        return $($("Birthday"), $(""), $("500"));
    }

    @Before
    public void setup() {
        attributeDao = new JEVisAttributeDAO();
        dataObject = new JEVisObjectBuilder().withAttribute("Value").withAttribute("Building").withAttribute("Members").withAttribute("Address").build();
    }

    @Test
    @Parameters(method = "getCorrectAttributeNames")
    public void shouldGiveAttributeForCorrectValues(String correctAttributeName) {
        JEVisAttribute attribute = attributeDao.getJEVisAttribute(dataObject, correctAttributeName);
        Assert.assertNotNull(attribute);
    }

    @Test
    @Parameters(method = "getIncorrectAttributeNames")
    public void shouldGiveNullForIncorrectValues(String incorrectAttributeName) {
        JEVisAttribute attribute = attributeDao.getJEVisAttribute(dataObject, incorrectAttributeName);
        Assert.assertNull(attribute);
    }

    @Test
    public void shouldGiveNullForINullValue() {
        JEVisAttribute attribute = attributeDao.getJEVisAttribute(dataObject, null);
        Assert.assertNull(attribute);
    }
}
