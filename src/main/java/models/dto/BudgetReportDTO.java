package models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private LocalDate period;
    private String type;
    private String categoryName;
    private BigDecimal plannedAmount;
    private BigDecimal factAmount;
    private BigDecimal deviation;
    private Double completionPercent;
    private String status;

    public BudgetReportDTO() {}

    public BudgetReportDTO(Integer id, LocalDate period, String type, String categoryName, BigDecimal plannedAmount, BigDecimal factAmount, BigDecimal deviation, Double completionPercent, String status) {
        this.id = id;
        this.period = period;
        this.type = type;
        this.categoryName = categoryName;
        this.plannedAmount = plannedAmount;
        this.factAmount = factAmount;
        this.deviation = deviation;
        this.completionPercent = completionPercent;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getPeriod() { return period; }
    public void setPeriod(LocalDate period) { this.period = period; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
    public BigDecimal getFactAmount() { return factAmount; }
    public void setFactAmount(BigDecimal factAmount) { this.factAmount = factAmount; }
    public BigDecimal getDeviation() { return deviation; }
    public void setDeviation(BigDecimal deviation) { this.deviation = deviation; }
    public Double getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(Double completionPercent) { this.completionPercent = completionPercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}