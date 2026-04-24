package models;

import models.entities.Budget;
import enums.BudgetStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetTest {

    @Test
    void testBudgetCreation() {

        Budget budget = new Budget();
        budget.setId(1);
        budget.setUserId(10);
        budget.setPeriod(LocalDate.of(2026, 4, 1));
        budget.setType("INCOME");
        budget.setPlannedAmount(new BigDecimal("10000.00"));
        budget.setStatus(BudgetStatus.DRAFT);

        assertEquals(1, budget.getId());
        assertEquals(10, budget.getUserId());
        assertEquals("INCOME", budget.getType());
        assertEquals(BudgetStatus.DRAFT, budget.getStatus());
    }

    @Test
    void testBudgetStatusTransition() {

        Budget budget = new Budget();
        budget.setStatus(BudgetStatus.DRAFT);

        budget.setStatus(BudgetStatus.APPROVED);

        assertEquals(BudgetStatus.APPROVED, budget.getStatus());
    }
}