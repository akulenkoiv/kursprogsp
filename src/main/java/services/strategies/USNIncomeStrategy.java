package services.strategies;

import models.entities.Expense;
import models.entities.Income;

import java.math.BigDecimal;
import java.util.List;

public class USNIncomeStrategy implements TaxCalculationStrategy {
    @Override
    public BigDecimal calculateTaxBase(List<Income> incomes, List<Expense> expenses) {
        return incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public double getRate() {
        return 0.06;
    }
}