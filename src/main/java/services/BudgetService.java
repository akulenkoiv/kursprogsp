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
        System.out.println("[Service] Запрос бюджетов: userId=" + userId + ", period=" + period);

        List<Budget> budgets = budgetDAO.findByUserIdAndPeriod(userId, period);
        System.out.println("[Service] Найдено в БД: " + budgets.size() + " бюджетов");

        List<BudgetReportDTO> reports = new ArrayList<>();

        for (Budget b : budgets) {
            System.out.println("[Service] Обработка бюджета ID=" + b.getId() + ", type=" + b.getType());

            BigDecimal factAmount = BigDecimal.ZERO;
            String categoryName = "Общий";

            if ("INCOME".equalsIgnoreCase(b.getType())) {
                if (b.getIncomeCategoryId() != null && b.getIncomeCategoryId() > 0) {
                    List<Income> incomes = transactionDAO.getIncomes(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    System.out.println("[Service] Доходов найдено: " + incomes.size());

                    for (Income i : incomes) {
                        if (i.getCategoryId() > 0 && i.getCategoryId() == b.getIncomeCategoryId()) {
                            factAmount = factAmount.add(i.getAmount());
                        }
                    }
                    categoryName = "Категория дохода ID: " + b.getIncomeCategoryId();
                } else {
                    List<Income> incomes = transactionDAO.getIncomes(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    for (Income i : incomes) factAmount = factAmount.add(i.getAmount());
                    categoryName = "Все доходы";
                }
            } else if ("EXPENSE".equalsIgnoreCase(b.getType())) {
                if (b.getExpenseCategoryId() != null && b.getExpenseCategoryId() > 0) {
                    List<Expense> expenses = transactionDAO.getExpenses(userId, period.withDayOfMonth(1), period.withDayOfMonth(period.lengthOfMonth()));
                    System.out.println("[Service] Расходов найдено: " + expenses.size());

                    for (Expense e : expenses) {
                        if (e.getCategoryId() > 0 && e.getCategoryId() == b.getExpenseCategoryId()) {
                            factAmount = factAmount.add(e.getAmount());
                        }
                    }
                    categoryName = "Категория расхода ID: " + b.getExpenseCategoryId();
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

            BudgetReportDTO dto = new BudgetReportDTO(
                    b.getId(), b.getPeriod(), b.getType(), categoryName,
                    b.getPlannedAmount(), factAmount, deviation, percent, b.getStatus().name()
            );

            System.out.println("[Service] Создан DTO: " + dto);
            reports.add(dto);
        }

        System.out.println("[Service] Всего отчётов: " + reports.size());
        return reports;
    }
}