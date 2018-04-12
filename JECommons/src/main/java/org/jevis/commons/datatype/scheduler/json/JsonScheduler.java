/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.datatype.scheduler.json;

import org.jevis.commons.datatype.scheduler.json.JsonSchedulerRule;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTimeZone;

/**
 * json scheduler
 * @author Artur Iablokov
 */
public class JsonScheduler {
    
    private String timezone;
    private List<JsonSchedulerRule> rules;

    public JsonScheduler(){
        rules = new ArrayList<>();
        timezone = DateTimeZone.getDefault().getID();
    }
    
    @JsonGetter("rules")
    public List<JsonSchedulerRule> getScheduler() {
        return rules;
    }
    
    @JsonSetter("rules")
    public void setScheduler(List<JsonSchedulerRule> scheduler) {
        this.rules = scheduler;
    }

    @JsonGetter("timezone")
    public String getTimezone() {
        return timezone;
    }

    @JsonSetter("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
