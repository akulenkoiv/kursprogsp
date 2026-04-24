package enums;

public enum BudgetType {
    MONTHLY("Ежемесячный"),
    QUARTERLY("Ежеквартальный"),
    YEARLY("Ежегодный");

    private final String description;

    BudgetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}