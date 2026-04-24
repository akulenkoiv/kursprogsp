package services;

import dao.TransactionDAO;
import dao.TaxPaymentDAO;
import dao.impl.TaxPaymentDAOImpl;
import dao.impl.TransactionDAOImpl;
import enums.PaymentStatus;
import models.entities.Expense;
import models.entities.Income;
import models.entities.TaxPayment;
import services.strategies.TaxCalculationStrategy;
import services.strategies.USNIncomeExpenseStrategy;
import services.strategies.USNIncomeStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxService {
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private final TaxPaymentDAO taxPaymentDAO = new TaxPaymentDAOImpl();
    private final Map<String, TaxCalculationStrategy> strategies = new HashMap<>();

    public TaxService() {
        strategies.put("USN_INCOME", new USNIncomeStrategy());
        strategies.put("USN_INCOME_EXPENSE", new USNIncomeExpenseStrategy());
    }

    public TaxPayment calculateTax(int userId, LocalDate start, LocalDate end, String regimeName) throws Exception {
        TaxCalculationStrategy strategy = strategies.get(regimeName);
        if (strategy == null) {
            throw new IllegalArgumentException("Неизвестный налоговый режим: " + regimeName);
        }

        List<Income> incomes = transactionDAO.getIncomes(userId, start, end);
        List<Expense> expenses = transactionDAO.getExpenses(userId, start, end);

        BigDecimal taxBase = strategy.calculateTaxBase(incomes, expenses);
        BigDecimal taxAmount = taxBase.multiply(BigDecimal.valueOf(strategy.getRate()))
                .setScale(2, RoundingMode.HALF_UP);

        TaxPayment payment = new TaxPayment();
        payment.setUserId(userId);
        payment.setPeriodStart(start);
        payment.setPeriodEnd(end);
        payment.setTaxType(regimeName);
        payment.setAmount(taxAmount);
        payment.setStatus(PaymentStatus.CALCULATED);

        TaxPayment existing = taxPaymentDAO.findByPeriod(userId, start, end, regimeName);
        if (existing != null) {
            payment.setId(existing.getId());
            taxPaymentDAO.update(payment);
        } else {
            taxPaymentDAO.create(payment);
        }

        return payment;
    }
}