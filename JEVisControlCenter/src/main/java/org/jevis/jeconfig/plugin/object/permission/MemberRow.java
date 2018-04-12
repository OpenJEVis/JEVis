/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.permission;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MemberRow {

    public SimpleStringProperty member = new SimpleStringProperty("*Unknown*");
    public SimpleBooleanProperty read = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty write = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty exce = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty create = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty delete = new SimpleBooleanProperty(false);

    public MemberRow(JEVisObject member, List<JEVisRelationship> rights) {

        try {
            this.member.setValue(member.getName());

            for (JEVisRelationship rel : rights) {
                switch (rel.getType()) {
                    case MEMBER_READ:
                        read.setValue(Boolean.TRUE);
                        break;
                    case MEMBER_CREATE:
                        create.setValue(Boolean.TRUE);
                        break;
                    case MEMBER_DELETE:
                        delete.setValue(Boolean.TRUE);
                        break;
                    case MEMBER_EXECUTE:
                        exce.setValue(Boolean.FALSE);
                        break;
                    case MEMBER_WRITE:
                        write.setValue(Boolean.TRUE);
                        break;
                    default:
                            ;
                        break;
                }

            }
        } catch (JEVisException ex) {
        }
    }

    public String getMember() {
        return member.getValue();
    }

    public void setMember(String member) {
        this.member.setValue(member);
    }

    public boolean getRead() {
        return read.getValue();
    }

    public void setRead(boolean read) {
        this.read.setValue(read);
    }

    public boolean getWrite() {
        return write.getValue();
    }

    public void setWrite(boolean write) {
        this.write.setValue(write);
    }

    public boolean getExce() {
        return exce.getValue();
    }

    public void setExce(boolean exce) {
        this.exce.setValue(exce);
    }

    public boolean getCreate() {
        return create.getValue();
    }

    public void setCreate(boolean create) {
        this.create.setValue(create);
    }

    public boolean getDelete() {
        return delete.getValue();
    }

    public void setDelete(boolean delete) {
        this.delete.setValue(delete);
    }

}
