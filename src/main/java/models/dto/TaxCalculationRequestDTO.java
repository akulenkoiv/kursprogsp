package models.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class TaxCalculationRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate startDate;
    private LocalDate endDate;
    private String regime;

    public TaxCalculationRequestDTO() {}

    public TaxCalculationRequestDTO(LocalDate startDate, LocalDate endDate, String regime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.regime = regime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }
}