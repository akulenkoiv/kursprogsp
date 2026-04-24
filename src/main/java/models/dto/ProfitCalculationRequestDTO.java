package models.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class ProfitCalculationRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate startDate;
    private LocalDate endDate;

    public ProfitCalculationRequestDTO() {}

    public ProfitCalculationRequestDTO(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}