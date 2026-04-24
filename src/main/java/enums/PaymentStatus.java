package enums;

public enum PaymentStatus {
    CALCULATED("Рассчитан"),
    PAID("Оплачен"),
    OVERDUE("Просрочен"),
    CANCELLED("Отменен");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}