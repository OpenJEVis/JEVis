package org.jevis.jeconfig.plugin.dtrc;

import org.jevis.api.*;

public class JEVisNameType implements JEVisType {

    private final JEVisDataSource ds;
    private final JEVisClass jeVisClass;


    public JEVisNameType(JEVisDataSource ds, JEVisClass jeVisClass) {
        super();
        this.ds = ds;
        this.jeVisClass = jeVisClass;
    }

    @Override
    public String getName() throws JEVisException {
        return "name";
    }

    @Override
    public void setName(String name) throws JEVisException {

    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return 0;
    }

    @Override
    public void setPrimitiveType(int type) throws JEVisException {

    }

    @Override
    public String getGUIDisplayType() throws JEVisException {
        return "Text";
    }

    @Override
    public void setGUIDisplayType(String type) throws JEVisException {

    }

    @Override
    public int getGUIPosition() throws JEVisException {
        return 0;
    }

    @Override
    public void setGUIPosition(int pos) throws JEVisException {

    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return jeVisClass;
    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return jeVisClass.getName();
    }

    @Override
    public int getValidity() throws JEVisException {
        return 0;
    }

    @Override
    public void setValidity(int validity) throws JEVisException {

    }

    @Override
    public String getConfigurationValue() throws JEVisException {
        return null;
    }

    @Override
    public void setConfigurationValue(String value) throws JEVisException {

    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return null;
    }

    @Override
    public void setUnit(JEVisUnit unit) throws JEVisException {

    }

    @Override
    public String getAlternativSymbol() throws JEVisException {
        return null;
    }

    @Override
    public void setAlternativSymbol(String symbol) throws JEVisException {

    }

    @Override
    public String getDescription() throws JEVisException {
        return null;
    }

    @Override
    public void setDescription(String discription) throws JEVisException {

    }

    @Override
    public boolean delete() throws JEVisException {
        return false;
    }

    @Override
    public boolean isInherited() throws JEVisException {
        return false;
    }

    @Override
    public void setInherited(boolean inherited) throws JEVisException {

    }

    @Override
    public int compareTo(JEVisType o) {
        return 0;
    }

    @Override
    public void commit() throws JEVisException {

    }

    @Override
    public void rollBack() throws JEVisException {

    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return ds;
    }
}
