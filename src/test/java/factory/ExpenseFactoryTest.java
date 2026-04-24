package factory;

import factory.impl.ExpenseFactory;
import models.entities.Expense;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public class ExpenseFactoryTest {

    @Test
    void testCreateExpense() {

        ExpenseFactory factory = new ExpenseFactory();
        BigDecimal amount = new BigDecimal("3000.00");
        LocalDate date = LocalDate.of(2026, 4, 10);
        String comment = "Закупка материалов";
        int userId = 1;
        int categoryId = 3;
        Integer counterpartyId = 7;


        Expense expense = factory.createExpense(
                0, date, amount, comment, userId, null, categoryId, counterpartyId
        );


        assertNotNull(expense, "Объект Expense не должен быть null");
        assertEquals(amount, expense.getAmount());
        assertEquals(date, expense.getDate());
        assertEquals(categoryId, expense.getCategoryId());
        assertEquals(counterpartyId, expense.getCounterpartyId());
    }

    @Test
    void testCreateExpense_WithoutCounterparty() {

        ExpenseFactory factory = new ExpenseFactory();


        Expense expense = factory.createExpense(
                2, LocalDate.now(), new BigDecimal("500"), "Коммуналка",
                1, null, 4, null
        );


        assertNull(expense.getCounterpartyId(),
                "ID контрагента должен быть null");
    }
}