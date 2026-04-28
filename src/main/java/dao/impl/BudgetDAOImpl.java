package dao.impl;

import dao.BudgetDAO;
import enums.BudgetStatus;
import models.entities.Budget;
import utility.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAOImpl implements BudgetDAO {

    @Override
    public List<Budget> findByUserId(int userId) throws Exception {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE user_id = ? ORDER BY period DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBudget(rs));
            }
        }
        return list;
    }

    @Override
    public Budget findById(int id) throws Exception {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBudget(rs);
            }
        }
        return null;
    }

    @Override
    public void create(Budget budget) throws Exception {
        String sql = "INSERT INTO budgets (user_id, period, type, income_category_id, expense_category_id, planned_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, budget.getUserId());
            ps.setDate(2, Date.valueOf(budget.getPeriod()));

            String typeToSave = (budget.getType() == null || budget.getType().isEmpty()) ? "INCOME" : budget.getType();
            ps.setString(3, typeToSave);

            if (budget.getIncomeCategoryId() != null && budget.getIncomeCategoryId() > 0) {
                ps.setInt(4, budget.getIncomeCategoryId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (budget.getExpenseCategoryId() != null && budget.getExpenseCategoryId() > 0) {
                ps.setInt(5, budget.getExpenseCategoryId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setBigDecimal(6, budget.getPlannedAmount());
            ps.setString(7, budget.getStatus() != null ? budget.getStatus().name() : "DRAFT");

            int rows = ps.executeUpdate();
            System.out.println("[DAO] Строк добавлено: " + rows);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    System.out.println("[DAO] Сгенерированный ID: " + generatedId);
                    budget.setId(generatedId);
                }
            }
        }
    }

    @Override
    public void update(Budget budget) throws Exception {
        String sql = "UPDATE budgets SET type = ?, income_category_id = ?, expense_category_id = ?, planned_amount = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String typeToSave = (budget.getType() == null || budget.getType().isEmpty()) ? "INCOME" : budget.getType();
            ps.setString(1, typeToSave);

            if (budget.getIncomeCategoryId() != null && budget.getIncomeCategoryId() > 0) {
                ps.setInt(2, budget.getIncomeCategoryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (budget.getExpenseCategoryId() != null && budget.getExpenseCategoryId() > 0) {
                ps.setInt(3, budget.getExpenseCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setBigDecimal(4, budget.getPlannedAmount());
            ps.setString(5, budget.getStatus() != null ? budget.getStatus().name() : "DRAFT");
            ps.setInt(6, budget.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }


    @Override
    public List<Budget> findByUserIdAndPeriod(int userId, LocalDate period) throws Exception {
        List<Budget> list = new ArrayList<>();

        String sql = "SELECT * FROM budgets WHERE user_id = ? AND MONTH(period) = ? AND YEAR(period) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, period.getMonthValue()); // Месяц (1-12)
            ps.setInt(3, period.getYear());       // Год

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBudget(rs));
            }
        }
        return list;
    }

    private Budget mapBudget(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setPeriod(rs.getDate("period").toLocalDate());
        b.setType(rs.getString("type"));
        b.setIncomeCategoryId(rs.getObject("income_category_id", Integer.class));
        b.setExpenseCategoryId(rs.getObject("expense_category_id", Integer.class));
        b.setPlannedAmount(rs.getBigDecimal("planned_amount"));

        String statusStr = rs.getString("status");
        b.setStatus(statusStr != null ? BudgetStatus.valueOf(statusStr) : BudgetStatus.DRAFT);
        return b;
    }
}