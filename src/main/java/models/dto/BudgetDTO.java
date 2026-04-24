package models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private LocalDate period;
    private String type;
    private Integer categoryId;
    private BigDecimal plannedAmount;
    private String status;

    public BudgetDTO() {}

    public BudgetDTO(Integer id, LocalDate period, String type, Integer categoryId, BigDecimal plannedAmount, String status) {
        this.id = id;
        this.period = period;
        this.type = type;
        this.categoryId = categoryId;
        this.plannedAmount = plannedAmount;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getPeriod() { return period; }
    public void setPeriod(LocalDate period) { this.period = period; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}