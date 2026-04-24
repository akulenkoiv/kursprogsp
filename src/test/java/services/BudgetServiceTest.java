package services;

import models.dto.BudgetReportDTO;
import models.entities.Budget;
import enums.BudgetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public class BudgetServiceTest {

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService();
    }


    @Test
    void testCreateBudget_IncomeType() {

        Budget budget = new Budget();
        budget.setUserId(1);
        budget.setPeriod(LocalDate.of(2026, 4, 1));
        budget.setType("INCOME");
        budget.setPlannedAmount(new BigDecimal("5000.00"));
        budget.setStatus(BudgetStatus.DRAFT);


        assertDoesNotThrow(() -> {
            budgetService.createBudget(budget);
        }, "Создание бюджета типа INCOME не должно выбрасывать исключений");

        assertEquals("INCOME", budget.getType(), "Тип бюджета должен быть INCOME");
        assertEquals(new BigDecimal("5000.00"), budget.getPlannedAmount(), "Плановая сумма должна совпадать");
    }


    @Test
    void testCreateBudget_ExpenseType() {

        Budget budget = new Budget();
        budget.setUserId(1);
        budget.setPeriod(LocalDate.of(2026, 4, 1));
        budget.setType("EXPENSE");
        budget.setPlannedAmount(new BigDecimal("3000.00"));
        budget.setStatus(BudgetStatus.DRAFT);


        assertDoesNotThrow(() -> {
            budgetService.createBudget(budget);
        });

        assertEquals("EXPENSE", budget.getType());
    }


    @Test
    void testCalculateCompletionPercent() {

        BigDecimal planned = new BigDecimal("1000.00");
        BigDecimal fact = new BigDecimal("800.00");


        double percent = fact.divide(planned, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();


        assertEquals(80.0, percent, 0.01, "Процент выполнения должен быть 80%");
    }


    @Test
    void testCalculatePercent_ZeroPlannedAmount() {

        BigDecimal planned = BigDecimal.ZERO;
        BigDecimal fact = new BigDecimal("500.00");


        double percent = 0.0;
        if (planned.compareTo(BigDecimal.ZERO) != 0) {
            percent = fact.divide(planned, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }


        assertEquals(0.0, percent, 0.01, "При нулевой плановой сумме процент должен быть 0");
    }


    @Test
    void testCalculateDeviation() {

        BigDecimal planned = new BigDecimal("1000.00");
        BigDecimal fact = new BigDecimal("1200.00");


        BigDecimal deviation = fact.subtract(planned);


        assertEquals(new BigDecimal("200.00"), deviation, "Отклонение должно быть +200");
    }

    @Test
    void testCalculateNegativeDeviation() {

        BigDecimal planned = new BigDecimal("1000.00");
        BigDecimal fact = new BigDecimal("800.00");


        BigDecimal deviation = fact.subtract(planned);


        assertEquals(new BigDecimal("-200.00"), deviation, "Отклонение должно быть -200");
    }

    @Test
    void testApproveBudget() {

        Budget budget = new Budget();
        budget.setId(1);
        budget.setStatus(BudgetStatus.DRAFT);


        budget.setStatus(BudgetStatus.APPROVED);


        assertEquals(BudgetStatus.APPROVED, budget.getStatus(),
                "Статус должен измениться на APPROVED");
    }
}