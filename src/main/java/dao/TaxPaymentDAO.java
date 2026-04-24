package dao;

import models.entities.TaxPayment;
import java.time.LocalDate;

public interface TaxPaymentDAO {
    void create(TaxPayment payment) throws Exception;
    void update(TaxPayment payment) throws Exception;
    TaxPayment findByPeriod(int userId, LocalDate start, LocalDate end, String type) throws Exception;
}