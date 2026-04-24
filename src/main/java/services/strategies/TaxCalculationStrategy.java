package services.strategies;

import models.entities.Expense;
import models.entities.Income;

import java.math.BigDecimal;
import java.util.List;

public interface TaxCalculationStrategy {
    BigDecimal calculateTaxBase(List<Income> incomes, List<Expense> expenses);
    double getRate();
}