/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ObjectTargetHelper {
    private Long id = 0L;
    private String name = new String();
    private String classname = new String();

    public ObjectTargetHelper(long id, String name, String jevisClass) {
        this.id = id;
        this.name = name;
        this.classname = jevisClass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    @Override
    public String toString() {
        return "ObjectTargetHelper{" + "id=" + id + ", name=" + name + ", classname=" + classname + '}';
    }


}
