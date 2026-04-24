package dao;

import models.entities.*;
import java.time.LocalDate;
import java.util.List;

public interface TransactionDAO {
    List<Income> getIncomes(int userId, LocalDate start, LocalDate end) throws Exception;
    List<Expense> getExpenses(int userId, LocalDate start, LocalDate end) throws Exception;
    void createIncome(Income income) throws Exception;
    void createExpense(Expense expense) throws Exception;
    void updateIncome(Income income) throws Exception;
    void updateExpense(Expense expense) throws Exception;
    void deleteIncome(int id) throws Exception;
    void deleteExpense(int id) throws Exception;

    List<IncomeCategory> getIncomeCategories() throws Exception;
    void createIncomeCategory(IncomeCategory category) throws Exception;
    void updateIncomeCategory(IncomeCategory category) throws Exception;
    void deleteIncomeCategory(int id) throws Exception;

    List<ExpenseCategory> getExpenseCategories() throws Exception;
    void createExpenseCategory(ExpenseCategory category) throws Exception;
    void updateExpenseCategory(ExpenseCategory category) throws Exception;
    void deleteExpenseCategory(int id) throws Exception;

    List<Counterparty> getCounterparties() throws Exception;
    void createCounterparty(Counterparty counterparty) throws Exception;
    void updateCounterparty(Counterparty counterparty) throws Exception;
    void deleteCounterparty(int id) throws Exception;
}