package org.jevis.jeconfig.plugin.object.extension.role;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.jevis.api.JEVisObject;

public class Membership {

    private final SimpleObjectProperty<JEVisObject> groupObject;
    private final SimpleStringProperty groupName;
    private final SimpleStringProperty groupid;
    private final SimpleBooleanProperty read;
    private final SimpleBooleanProperty write;
    private final SimpleBooleanProperty execute;
    private final SimpleBooleanProperty create;
    private final SimpleBooleanProperty delete;
    private final SimpleBooleanProperty hasChanged;
    private int initHash;

    public Membership(JEVisObject groupObject, boolean read, boolean write, boolean execute, boolean create, boolean delete) {
        this.groupObject = new SimpleObjectProperty<>(groupObject);
        this.groupName = new SimpleStringProperty(getFullPath(groupObject));
        this.groupid = new SimpleStringProperty(groupObject.getID().toString());
        this.read = new SimpleBooleanProperty(read);
        this.write = new SimpleBooleanProperty(write);
        this.execute = new SimpleBooleanProperty(execute);
        this.create = new SimpleBooleanProperty(create);
        this.delete = new SimpleBooleanProperty(delete);
        this.hasChanged=new SimpleBooleanProperty(false);


        //initHash= createHash();

        /**
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("initHash: "+initHash);
                System.out.println("new Hash: "+createHash());
                hasChanged.setValue(createHash()!=initHash);
                System.out.println("Change: "+groupObject.getID()+" "+hasChanged.get());
            }
        };
        this.read.addListener(changeListener);
        this.write.addListener(changeListener);
        this.execute.addListener(changeListener);
        this.create.addListener(changeListener);
        this.delete.addListener(changeListener);
        **/

    }

    public boolean hasChanged(){
        return createHash()!=initHash;
    }

    public void createInitHash(){
        initHash=createHash();
    }

    private int createHash(){
        return (""+read.getValue()+write.getValue()+execute.getValue()+create.getValue()+delete.getValue()).hashCode();
    }


    private String getFullPath(JEVisObject group){

        try{
            //TODO: replace with intelligent code
            return group.getParents().get(0).getParents().get(0).getParents().get(0).getName()+" / "+group.getName();
        }catch (Exception ex){
            return group.getName();
        }
    }


    public String getGroupName() {
        return groupName.get();
    }

    public SimpleStringProperty groupNameProperty() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName.set(groupName);
    }

    public JEVisObject getGroupObject() {
        return groupObject.get();
    }

    public SimpleObjectProperty<JEVisObject> groupObjectProperty() {
        return groupObject;
    }

    public void setGroupObject(JEVisObject groupObject) {
        this.groupObject.set(groupObject);
    }

    public String getGroupid() {
        return groupid.get();
    }

    public SimpleStringProperty groupidProperty() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid.set(groupid);
    }

    public boolean isRead() {
        return read.get();
    }

    public SimpleBooleanProperty readProperty() {
        return read;
    }

    public void setRead(boolean read) {
        this.read.set(read);
    }

    public boolean isWrite() {
        return write.get();
    }

    public SimpleBooleanProperty writeProperty() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write.set(write);
    }

    public boolean isExecute() {
        return execute.get();
    }

    public SimpleBooleanProperty executeProperty() {
        return execute;
    }

    public void setExecute(boolean execute) {
        this.execute.set(execute);
    }

    public boolean isCreate() {
        return create.get();
    }

    public SimpleBooleanProperty createProperty() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create.set(create);
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete.set(delete);
    }

    @Override
    public String toString() {
        return "Membership{" +
                ", groupName=" + groupName +
                ", groupid=" + groupid +
                ", read=" + read +
                ", write=" + write +
                ", execute=" + execute +
                ", create=" + create +
                ", delete=" + delete +
                ", initHash=" + initHash +
                '}';
    }
}
