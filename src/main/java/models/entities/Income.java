package models.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Income extends Transaction {
    private static final long serialVersionUID = 1L;

    private int categoryId;
    private Integer counterpartyId;

    public Income() {
        super();
    }

    public Income(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId) {
        super(id, date, amount, comment, userId, filePath);
        this.categoryId = categoryId;
        this.counterpartyId = counterpartyId;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Integer getCounterpartyId() { return counterpartyId; }
    public void setCounterpartyId(Integer counterpartyId) { this.counterpartyId = counterpartyId; }

    @Override
    public String toString() {
        return "Income{id=" + getId() + ", date=" + getDate() + ", amount=" + getAmount() + "}";
    }
}