package org.jevis.jeconfig.plugin.object.extension.role;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Role {

    private ObservableList<Membership> memberships = FXCollections.observableArrayList();
    private ObservableList<User> users =FXCollections.observableArrayList();
    private final JEVisObject roleObject;

    public ObservableList<Membership> getMemberships() {
        return memberships;
    }
    public ObservableList<User> getUsers() {
        return users;
    }
    public void setUsers(ObservableList<User> users) {
        this.users = users;
    }

    public Role(JEVisObject roleObject) {
        this.roleObject = roleObject;
    }

    public JEVisObject getRoleObject() {
        return roleObject;
    }

    public boolean hasChanged(){
        AtomicBoolean userChaned = new AtomicBoolean(false);
        AtomicBoolean membershipChanged = new AtomicBoolean(false);

        users.forEach(user -> {
            if (user.getInitMember() != user.memberProperty().get()) userChaned.set(true);
        });
        membershipChanged.set(!getChangeMemberships().isEmpty());
        return (membershipChanged.get()|| userChaned.get());
    }

    public List<Membership> getChangeMemberships(){
        List<Membership> changedList = new ArrayList<>();
        getMemberships().forEach(membership -> {
            if(membership.hasChanged()){
                changedList.add(membership);
            }
        });




        return changedList;
    }
}
