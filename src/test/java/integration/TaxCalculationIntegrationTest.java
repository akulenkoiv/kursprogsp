package integration;

import dao.TransactionDAO;
import dao.TaxPaymentDAO;
import dao.impl.TransactionDAOImpl;
import dao.impl.TaxPaymentDAOImpl;
import enums.PaymentStatus;
import models.entities.Income;
import models.entities.Expense;
import models.entities.TaxPayment;
import org.junit.jupiter.api.*;
import utility.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaxCalculationIntegrationTest {

    private static TransactionDAO transactionDAO;
    private static TaxPaymentDAO taxPaymentDAO;
    private static int testUserId;

    @BeforeAll
    static void setUp() throws Exception {
        transactionDAO = new TransactionDAOImpl();
        taxPaymentDAO = new TaxPaymentDAOImpl();
        testUserId = 1;
        cleanupTestData();
    }

    @AfterAll
    static void tearDown() throws Exception {
        cleanupTestData();
    }

    @Test
    @Order(1)
    @DisplayName("Регистрация доходов за период")
    void testCreateIncomes_AndVerifyInDatabase() throws Exception {
        Income income1 = new Income();
        income1.setUserId(testUserId);
        income1.setDate(LocalDate.of(2026, 4, 5));
        income1.setAmount(new BigDecimal("10000.00"));
        income1.setCategoryId(1);
        income1.setComment("Продажа товаров");

        Income income2 = new Income();
        income2.setUserId(testUserId);
        income2.setDate(LocalDate.of(2026, 4, 15));
        income2.setAmount(new BigDecimal("5000.00"));
        income2.setCategoryId(1);
        income2.setComment("Услуги");

        transactionDAO.createIncome(income1);
        transactionDAO.createIncome(income2);

        List<Income> incomes = transactionDAO.getIncomes(
                testUserId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );

        assertNotNull(incomes);
        assertEquals(2, incomes.size());

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("15000.00"), totalIncome);
    }

    @Test
    @Order(2)
    @DisplayName("Регистрация расходов за период")
    void testCreateExpenses_AndVerifyInDatabase() throws Exception {
        Expense expense1 = new Expense();
        expense1.setUserId(testUserId);
        expense1.setDate(LocalDate.of(2026, 4, 10));
        expense1.setAmount(new BigDecimal("3000.00"));
        expense1.setCategoryId(1);
        expense1.setComment("Закупка товаров");

        Expense expense2 = new Expense();
        expense2.setUserId(testUserId);
        expense2.setDate(LocalDate.of(2026, 4, 20));
        expense2.setAmount(new BigDecimal("2000.00"));
        expense2.setCategoryId(2);
        expense2.setComment("Аренда");

        transactionDAO.createExpense(expense1);
        transactionDAO.createExpense(expense2);

        List<Expense> expenses = transactionDAO.getExpenses(
                testUserId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );

        assertEquals(2, expenses.size());

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("5000.00"), totalExpense);
    }

    @Test
    @Order(3)
    @DisplayName("Расчёт налога УСН 'Доходы' (6%)")
    void testCalculateTax_USN_Income_6Percent() throws Exception {
        BigDecimal totalIncome = new BigDecimal("15000.00");
        BigDecimal expectedTax = totalIncome.multiply(new BigDecimal("0.06"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        TaxPayment taxPayment = new TaxPayment();
        taxPayment.setUserId(testUserId);
        taxPayment.setPeriodStart(LocalDate.of(2026, 4, 1));
        taxPayment.setPeriodEnd(LocalDate.of(2026, 4, 30));
        taxPayment.setTaxType("USN_INCOME");
        taxPayment.setAmount(expectedTax);
        taxPayment.setStatus(PaymentStatus.CALCULATED);

        taxPaymentDAO.create(taxPayment);

        TaxPayment saved = taxPaymentDAO.findByPeriod(
                testUserId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                "USN_INCOME"
        );

        assertNotNull(saved);
        assertEquals(new BigDecimal("900.00"), saved.getAmount());
        assertEquals(PaymentStatus.CALCULATED, saved.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("Расчёт налога УСН 'Доходы-Расходы' (15%)")
    void testCalculateTax_USN_IncomeExpense_15Percent() throws Exception {
        BigDecimal totalIncome = new BigDecimal("15000.00");
        BigDecimal totalExpense = new BigDecimal("5000.00");
        BigDecimal taxBase = totalIncome.subtract(totalExpense);
        BigDecimal expectedTax = taxBase.multiply(new BigDecimal("0.15"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        TaxPayment taxPayment = new TaxPayment();
        taxPayment.setUserId(testUserId);
        taxPayment.setPeriodStart(LocalDate.of(2026, 5, 1));
        taxPayment.setPeriodEnd(LocalDate.of(2026, 5, 31));
        taxPayment.setTaxType("USN_INCOME_EXPENSE");
        taxPayment.setAmount(expectedTax);
        taxPayment.setStatus(PaymentStatus.CALCULATED);

        taxPaymentDAO.create(taxPayment);

        TaxPayment saved = taxPaymentDAO.findByPeriod(
                testUserId,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "USN_INCOME_EXPENSE"
        );

        assertNotNull(saved);
        assertEquals(new BigDecimal("1500.00"), saved.getAmount());
    }

    @Test
    @Order(5)
    @DisplayName("Перерасчёт налога и обновление записи")
    void testRecalculateTax_AndUpdateRecord() throws Exception {
        TaxPayment existing = taxPaymentDAO.findByPeriod(
                testUserId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                "USN_INCOME"
        );

        assertNotNull(existing);
        BigDecimal newAmount = new BigDecimal("1200.00");
        existing.setAmount(newAmount);

        taxPaymentDAO.update(existing);

        TaxPayment updated = taxPaymentDAO.findByPeriod(
                testUserId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                "USN_INCOME"
        );

        assertEquals(newAmount, updated.getAmount());
    }

    @Test
    @Order(6)
    @DisplayName("Проверка ссылочной целостности с таблицей users")
    void testReferentialIntegrity_WithUsersTable() throws Exception {
        TaxPayment invalidPayment = new TaxPayment();
        invalidPayment.setUserId(99999);
        invalidPayment.setPeriodStart(LocalDate.of(2026, 6, 1));
        invalidPayment.setPeriodEnd(LocalDate.of(2026, 6, 30));
        invalidPayment.setTaxType("USN_INCOME");
        invalidPayment.setAmount(BigDecimal.TEN);
        invalidPayment.setStatus(PaymentStatus.CALCULATED);

        assertThrows(Exception.class, () -> {
            taxPaymentDAO.create(invalidPayment);
        });
    }

    private static void cleanupTestData() throws Exception {
        String deleteTax = "DELETE FROM tax_payments WHERE user_id = ? AND period_start >= ?";
        String deleteExpenses = "DELETE FROM expenses WHERE user_id = ? AND date >= ?";
        String deleteIncomes = "DELETE FROM incomes WHERE user_id = ? AND date >= ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteTax)) {
                ps.setInt(1, testUserId);
                ps.setDate(2, java.sql.Date.valueOf(LocalDate.of(2026, 4, 1)));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteExpenses)) {
                ps.setInt(1, testUserId);
                ps.setDate(2, java.sql.Date.valueOf(LocalDate.of(2026, 4, 1)));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteIncomes)) {
                ps.setInt(1, testUserId);
                ps.setDate(2, java.sql.Date.valueOf(LocalDate.of(2026, 4, 1)));
                ps.executeUpdate();
            }
        }
    }
}