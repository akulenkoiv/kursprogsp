package enums;

public enum Roles {
    ADMIN(3),
    ENTREPRENEUR(2),
    ACCOUNTANT(1);

    private final int level;

    Roles(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }

    public boolean canManageReferences() {
        return this == ADMIN || this == ENTREPRENEUR;
    }

    public boolean canManageBudgets() {
        return this == ADMIN || this == ENTREPRENEUR;
    }

    public boolean canRegisterUsers() {
        return this == ADMIN || this == ENTREPRENEUR;
    }

    public boolean canViewReports() {
        return true;
    }

    public boolean canManageTransactions() {
        return true;
    }
}