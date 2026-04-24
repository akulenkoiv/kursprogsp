package services;

import dao.TransactionDAO;
import dao.impl.TransactionDAOImpl;
import factory.TransactionFactory;
import factory.impl.ExpenseFactory;
import factory.impl.IncomeFactory;
import models.dto.ProfitCalculationResponseDTO;
import models.dto.ProfitLossReportDTO;
import models.entities.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class TransactionService {
    private final TransactionDAO dao = new TransactionDAOImpl();
    private final TransactionFactory incomeFactory = new IncomeFactory();
    private final TransactionFactory expenseFactory = new ExpenseFactory();

    public Income registerIncome(LocalDate date, BigDecimal amount, String comment, int userId, int categoryId, Integer counterpartyId) throws Exception {
        Income income = incomeFactory.createIncome(0, date, amount, comment, userId, null, categoryId, counterpartyId);
        dao.createIncome(income);
        return income;
    }

    public List<Income> getIncomes(int userId, LocalDate start, LocalDate end) throws Exception {
        return dao.getIncomes(userId, start, end);
    }

    public void updateIncome(int id, LocalDate date, BigDecimal amount, String comment, int categoryId, Integer counterpartyId) throws Exception {
        Income income = incomeFactory.createIncome(id, date, amount, comment, 0, null, categoryId, counterpartyId);
        dao.updateIncome(income);
    }

    public void deleteIncome(int id) throws Exception {
        dao.deleteIncome(id);
    }

    public Expense registerExpense(LocalDate date, BigDecimal amount, String comment, int userId, int categoryId, Integer counterpartyId) throws Exception {
        Expense expense = expenseFactory.createExpense(0, date, amount, comment, userId, null, categoryId, counterpartyId);
        dao.createExpense(expense);
        return expense;
    }

    public List<Expense> getExpenses(int userId, LocalDate start, LocalDate end) throws Exception {
        return dao.getExpenses(userId, start, end);
    }

    public void updateExpense(int id, LocalDate date, BigDecimal amount, String comment, int categoryId, Integer counterpartyId) throws Exception {
        Expense expense = expenseFactory.createExpense(id, date, amount, comment, 0, null, categoryId, counterpartyId);
        dao.updateExpense(expense);
    }

    public void deleteExpense(int id) throws Exception {
        dao.deleteExpense(id);
    }

    public ProfitCalculationResponseDTO calculateProfit(int userId, LocalDate start, LocalDate end) throws Exception {
        List<Income> incomes = dao.getIncomes(userId, start, end);
        List<Expense> expenses = dao.getExpenses(userId, start, end);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalIncome.subtract(totalExpense);

        return new ProfitCalculationResponseDTO(totalIncome, totalExpense, profit, start, end);
    }

    public ProfitLossReportDTO generateProfitLossReport(int userId, LocalDate start, LocalDate end) throws Exception {
        List<Income> incomes = dao.getIncomes(userId, start, end);
        List<Expense> expenses = dao.getExpenses(userId, start, end);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalIncome.subtract(totalExpense);
        BigDecimal taxAmount = totalIncome.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_UP);

        return new ProfitLossReportDTO(start, end, totalIncome, totalExpense, profit, taxAmount);
    }

    public List<IncomeCategory> getIncomeCategories() throws Exception { return dao.getIncomeCategories(); }
    public void saveIncomeCategory(IncomeCategory cat) throws Exception { dao.createIncomeCategory(cat); }
    public void updateIncomeCategory(IncomeCategory cat) throws Exception { dao.updateIncomeCategory(cat); }
    public void deleteIncomeCategory(int id) throws Exception { dao.deleteIncomeCategory(id); }

    public List<ExpenseCategory> getExpenseCategories() throws Exception { return dao.getExpenseCategories(); }
    public void saveExpenseCategory(ExpenseCategory cat) throws Exception { dao.createExpenseCategory(cat); }
    public void updateExpenseCategory(ExpenseCategory cat) throws Exception { dao.updateExpenseCategory(cat); }
    public void deleteExpenseCategory(int id) throws Exception { dao.deleteExpenseCategory(id); }

    public List<Counterparty> getCounterparties() throws Exception { return dao.getCounterparties(); }
    public void saveCounterparty(Counterparty cp) throws Exception { dao.createCounterparty(cp); }
    public void updateCounterparty(Counterparty cp) throws Exception { dao.updateCounterparty(cp); }
    public void deleteCounterparty(int id) throws Exception { dao.deleteCounterparty(id); }
}