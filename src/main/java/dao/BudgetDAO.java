package dao;

import models.entities.Budget;
import java.time.LocalDate;
import java.util.List;

public interface BudgetDAO {
    List<Budget> findByUserId(int userId) throws Exception;
    Budget findById(int id) throws Exception;
    void create(Budget budget) throws Exception;
    void update(Budget budget) throws Exception;
    void delete(int id) throws Exception;
    List<Budget> findByUserIdAndPeriod(int userId, LocalDate period) throws Exception;
}