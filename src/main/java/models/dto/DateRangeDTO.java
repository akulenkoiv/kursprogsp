package models.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class DateRangeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate start;
    private LocalDate end;

    public DateRangeDTO() {}

    public DateRangeDTO(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }
}