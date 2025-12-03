package at.qe.skeleton.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enumeration of available user roles.
 *
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
*/
public enum UserxRole implements GrantedAuthority {

    ADMIN,
    MANAGER,
    EMPLOYEE;

    @Override
    public String getAuthority() {
        return name();
    }
}
