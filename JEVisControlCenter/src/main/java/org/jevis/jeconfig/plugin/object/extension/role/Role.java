package org.jevis.jeconfig.plugin.object.extension.role;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Role {

    private final JEVisObject roleObject;
    private final ObservableList<Membership> memberShips = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();

    public Role(JEVisObject roleObject) {
        this.roleObject = roleObject;
    }

    public ObservableList<Membership> getMemberShips() {
        return memberShips;
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public void setUsers(ObservableList<User> users) {
        this.users = users;
    }

    public JEVisObject getRoleObject() {
        return roleObject;
    }

    public boolean hasChanged() {
        AtomicBoolean userChanged = new AtomicBoolean(false);
        AtomicBoolean membershipChanged = new AtomicBoolean(false);

        users.forEach(user -> {
            if (user.getInitMember() != user.memberProperty().get()) userChanged.set(true);
        });
        membershipChanged.set(!getChangeMemberships().isEmpty());
        return (membershipChanged.get() || userChanged.get());
    }

    public List<Membership> getChangeMemberships() {
        List<Membership> changedList = new ArrayList<>();
        getMemberShips().forEach(membership -> {
            if (membership.hasChanged()) {
                changedList.add(membership);
            }
        });


        return changedList;
    }
}
