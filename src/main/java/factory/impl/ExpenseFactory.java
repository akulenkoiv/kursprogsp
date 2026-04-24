package factory.impl;

import factory.TransactionFactory;
import models.entities.Expense;
import models.entities.Income;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseFactory implements TransactionFactory {
    @Override
    public Income createIncome(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId) {
        return null;
    }

    @Override
    public Expense createExpense(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId) {
        return new Expense(id, date, amount, comment, userId, filePath, categoryId, counterpartyId);
    }
}