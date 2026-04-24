package factory;

import factory.impl.IncomeFactory;
import models.entities.Income;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeFactoryTest {

    @Test
    void testCreateIncome() {

        IncomeFactory factory = new IncomeFactory();
        BigDecimal amount = new BigDecimal("5000.00");
        LocalDate date = LocalDate.of(2026, 4, 15);
        String comment = "Продажа товаров";
        int userId = 1;
        int categoryId = 2;


        Income income = factory.createIncome(
                0, date, amount, comment, userId, null, categoryId, null
        );


        assertNotNull(income, "Объект Income не должен быть null");
        assertEquals(amount, income.getAmount(), "Сумма должна совпадать");
        assertEquals(date, income.getDate(), "Дата должна совпадать");
        assertEquals(comment, income.getComment(), "Комментарий должен совпадать");
        assertEquals(categoryId, income.getCategoryId(), "ID категории должен совпадать");
    }

    @Test
    void testCreateIncome_WithCounterparty() {

        IncomeFactory factory = new IncomeFactory();
        Integer counterpartyId = 5;


        Income income = factory.createIncome(
                1, LocalDate.now(), new BigDecimal("1000"), "Тест",
                1, null, 1, counterpartyId
        );


        assertEquals(counterpartyId, income.getCounterpartyId(),
                "ID контрагента должен совпадать");
    }
}