package utility;

import enums.Roles;
import models.entities.User;

public class RoleChecker {

    private RoleChecker() {}

    public static boolean hasRole(User user, Roles requiredRole) {
        if (user == null) return false;
        return user.getRoleId() == requiredRole.getLevel();
    }

    public static boolean hasAnyRole(User user, Roles... roles) {
        if (user == null) return false;
        for (Roles role : roles) {
            if (user.getRoleId() == role.getLevel()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canManageUsers(User user) {
        return hasRole(user, Roles.ADMIN);
    }

    public static boolean canManageReferences(User user) {
        return hasAnyRole(user, Roles.ADMIN, Roles.ENTREPRENEUR);
    }

    public static boolean canManageBudgets(User user) {
        return hasAnyRole(user, Roles.ADMIN, Roles.ENTREPRENEUR);
    }

    public static boolean canRegisterUsers(User user) {
        return hasAnyRole(user, Roles.ADMIN, Roles.ENTREPRENEUR);
    }

    public static boolean canViewReports(User user) {
        return user != null;
    }

    public static boolean canManageTransactions(User user) {
        return user != null;
    }
}