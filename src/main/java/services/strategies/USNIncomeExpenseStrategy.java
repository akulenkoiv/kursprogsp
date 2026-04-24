package services.strategies;

import models.entities.Expense;
import models.entities.Income;

import java.math.BigDecimal;
import java.util.List;

public class USNIncomeExpenseStrategy implements TaxCalculationStrategy {
    @Override
    public BigDecimal calculateTaxBase(List<Income> incomes, List<Expense> expenses) {
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal base = totalIncome.subtract(totalExpense);
        return base.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : base;
    }

    @Override
    public double getRate() {
        return 0.15;
    }
}