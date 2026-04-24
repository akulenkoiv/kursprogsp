package services.strategies;

import java.math.BigDecimal;
import java.util.List;
import models.entities.Expense;
import models.entities.Income;

public interface TaxStrategy {
    BigDecimal calculate(List<Income> incomes, List<Expense> expenses);
    double getRate();
}