package services;

import dao.BudgetDAO;
import dao.TransactionDAO;
import dao.impl.BudgetDAOImpl;
import dao.impl.TransactionDAOImpl;
import enums.BudgetStatus;
import models.dto.BudgetReportDTO;
import models.entities.Budget;
import models.entities.Expense;
import models.entities.Income;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final BudgetDAO budgetDAO = new BudgetDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

    public List<Budget> getUserBudgets(int userId) throws Exception {
        return budgetDAO.findByUserId(userId);
    }

    public void createBudget(Budget budget) throws Exception {
        budgetDAO.create(budget);
    }

    public void updateBudget(Budget budget) throws Exception {
        Budget existing = budgetDAO.findById(budget.getId());
        if (existing == null) throw new IllegalArgumentException("Budget not found");
        if (!existing.getStatus().name().equals("DRAFT")) {
            throw new IllegalStateException("Can only edit DRAFT budgets");
        }
        budgetDAO.update(budget);
    }

    public void deleteBudget(int id) throws Exception {
        Budget existing = budgetDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Budget not found");
        if (!existing.getStatus().name().equals("DRAFT")) {
            throw new IllegalStateException("Can only delete DRAFT budgets");
        }
        budgetDAO.delete(id);
    }

    public void approveBudget(int id) throws Exception {
        Budget budget = budgetDAO.findById(id);
        if (budget == null) throw new IllegalArgumentException("Budget not found");
        budget.setStatus(BudgetStatus.APPROVED);
        budgetDAO.update(budget);
    }

    public List<BudgetReportDTO> generateBudgetReport(int userId, LocalDate period) throws Exception {
        List<Budget> budgets = budgetDAO.findByUserIdAndPeriod(userId, period);
        List<BudgetReportDTO> reports = new ArrayList<>();

        for (Budget b : budgets) {
            BigDecimal factAmount = BigDecimal.ZERO;
            String categoryName = "Общий";

            System.out.println("=== ОБРАБОТКА БЮДЖЕТА ===");
            System.out.println("Budget ID: " + b.getId());
            System.out.println("Type: " + b.getType());
            System.out.println("Income cat ID: " + b.getIncomeCategoryId());
            System.out.println("Expense cat ID: " + b.getExpenseCategoryId());

            if ("INCOME".equals(b.getType())) {

                if (b.getIncomeCategoryId() > 0) {
                    List<Income> incomes = transactionDAO.getIncomes(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    for (Income i : incomes) {
                        if (i.getCategoryId() == b.getIncomeCategoryId()) {
                            factAmount = factAmount.add(i.getAmount());
                        }
                    }
                    categoryName = "Категория ID: " + b.getIncomeCategoryId();
                } else {
                    List<Income> incomes = transactionDAO.getIncomes(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    for (Income i : incomes) factAmount = factAmount.add(i.getAmount());
                    categoryName = "Все доходы";
                }
            } else if ("EXPENSE".equals(b.getType())) {

                if (b.getExpenseCategoryId() > 0) {
                    List<Expense> expenses = transactionDAO.getExpenses(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    for (Expense e : expenses) {
                        if (e.getCategoryId() == b.getExpenseCategoryId()) {
                            factAmount = factAmount.add(e.getAmount());
                        }
                    }
                    categoryName = "Категория ID: " + b.getExpenseCategoryId();
                } else {
                    List<Expense> expenses = transactionDAO.getExpenses(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    for (Expense e : expenses) factAmount = factAmount.add(e.getAmount());
                    categoryName = "Все расходы";
                }
            }

            BigDecimal deviation = factAmount.subtract(b.getPlannedAmount());
            double percent = 0.0;
            if (b.getPlannedAmount().compareTo(BigDecimal.ZERO) != 0) {
                percent = factAmount.divide(b.getPlannedAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }

            reports.add(new BudgetReportDTO(
                    b.getId(), b.getPeriod(), b.getType(), categoryName,
                    b.getPlannedAmount(), factAmount, deviation, percent, b.getStatus().name()
            ));
        }
        return reports;
    }
}