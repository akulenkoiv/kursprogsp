package enums;

public enum BudgetStatus {
    DRAFT("Черновик"),
    APPROVED("Утвержден"),
    IN_PROGRESS("В выполнении"),
    COMPLETED("Завершен"),
    CANCELLED("Отменен");

    private final String description;

    BudgetStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}