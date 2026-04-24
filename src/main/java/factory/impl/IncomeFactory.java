package factory.impl;

import factory.TransactionFactory;
import models.entities.Income;
import models.entities.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeFactory implements TransactionFactory {
    @Override
    public Income createIncome(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId) {
        return new Income(id, date, amount, comment, userId, filePath, categoryId, counterpartyId);
    }

    @Override
    public Expense createExpense(int id, LocalDate date, BigDecimal amount, String comment, int userId, String filePath, int categoryId, Integer counterpartyId) {
        return null;
    }
}