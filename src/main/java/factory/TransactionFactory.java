package factory;

import models.entities.Income;
import models.entities.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionFactory {
    Income createIncome(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId);
    Expense createExpense(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId);
}