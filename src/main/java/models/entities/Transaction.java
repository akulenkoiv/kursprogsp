package models.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected LocalDate date;
    protected BigDecimal amount;
    protected String comment;
    protected int userId;
    protected String filePath;

    public Transaction() {}

    public Transaction(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.comment = comment;
        this.userId = userId;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}