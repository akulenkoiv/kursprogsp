package models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateTransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private LocalDate date;
    private BigDecimal amount;
    private String comment;
    private int categoryId;
    private Integer counterpartyId;

    public UpdateTransactionDTO() {}

    public UpdateTransactionDTO(int id, LocalDate date, BigDecimal amount, String comment, int categoryId, Integer counterpartyId) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.comment = comment;
        this.categoryId = categoryId;
        this.counterpartyId = counterpartyId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(Integer counterpartyId) {
        this.counterpartyId = counterpartyId;
    }
}