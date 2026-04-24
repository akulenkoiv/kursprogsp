package services;

import models.entities.Income;
import models.entities.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaxCalculationServiceTest {

    private List<Income> incomes;
    private List<Expense> expenses;

    @BeforeEach
    void setUp() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    @Test
    void testCalculateTax_USN_Income() {

        Income income1 = createIncome(new BigDecimal("10000.00"));
        Income income2 = createIncome(new BigDecimal("5000.00"));
        incomes.add(income1);
        incomes.add(income2);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal tax = totalIncome.multiply(new BigDecimal("0.06"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);


        assertEquals(new BigDecimal("900.00"), tax,
                "Налог УСН 6% от 15000 должен быть 900.00");
    }

    @Test
    void testCalculateTax_USN_IncomeExpense() {

        Income income = createIncome(new BigDecimal("20000.00"));
        Expense expense1 = createExpense(new BigDecimal("8000.00"));
        Expense expense2 = createExpense(new BigDecimal("2000.00"));

        incomes.add(income);
        expenses.add(expense1);
        expenses.add(expense2);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal taxBase = totalIncome.subtract(totalExpense);
        BigDecimal tax = taxBase.multiply(new BigDecimal("0.15"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);


        assertEquals(new BigDecimal("1500.00"), tax,
                "Налог УСН 15% от базы 10000 должен быть 1500.00");
    }


    @Test
    void testCalculateTax_ExpensesExceedIncome() {

        Income income = createIncome(new BigDecimal("5000.00"));
        Expense expense = createExpense(new BigDecimal("8000.00"));

        incomes.add(income);
        expenses.add(expense);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal taxBase = totalIncome.subtract(totalExpense);
        if (taxBase.compareTo(BigDecimal.ZERO) < 0) {
            taxBase = BigDecimal.ZERO;
        }
        BigDecimal tax = taxBase.multiply(new BigDecimal("0.15"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);


        assertEquals(0, tax.compareTo(BigDecimal.ZERO), "При убытке налог должен быть 0");
    }

    @Test
    void testCalculateProfit() {

        Income income = createIncome(new BigDecimal("15000.00"));
        Expense expense1 = createExpense(new BigDecimal("6000.00"));
        Expense expense2 = createExpense(new BigDecimal("3000.00"));

        incomes.add(income);
        expenses.add(expense1);
        expenses.add(expense2);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal profit = totalIncome.subtract(totalExpense);


        assertEquals(new BigDecimal("6000.00"), profit,
                "Прибыль должна быть 6000.00");
    }


    @Test
    void testTaxRounding() {

        BigDecimal income = new BigDecimal("10000.33");


        BigDecimal tax = income.multiply(new BigDecimal("0.06"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);


        assertEquals(new BigDecimal("600.02"), tax,
                "Налог должен быть округлён до 600.02");
    }


    private Income createIncome(BigDecimal amount) {
        Income income = new Income();
        income.setAmount(amount);
        income.setDate(LocalDate.of(2026, 4, 1));
        return income;
    }

    private Expense createExpense(BigDecimal amount) {
        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setDate(LocalDate.of(2026, 4, 1));
        return expense;
    }
}