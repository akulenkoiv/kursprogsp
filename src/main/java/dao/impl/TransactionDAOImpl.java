package dao.impl;

import dao.TransactionDAO;
import factory.impl.ExpenseFactory;
import factory.impl.IncomeFactory;
import models.entities.*;
import utility.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public List<Income> getIncomes(int userId, LocalDate start, LocalDate end) throws Exception {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM incomes WHERE user_id = ? AND date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapIncome(rs));
            }
        }
        return list;
    }

    @Override
    public List<Expense> getExpenses(int userId, LocalDate start, LocalDate end) throws Exception {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapExpense(rs));
            }
        }
        return list;
    }

    @Override
    public void createIncome(Income income) throws Exception {
        String sql = "INSERT INTO incomes (date, amount, category_id, counterparty_id, user_id, comment, file_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(income.getDate()));
            ps.setBigDecimal(2, income.getAmount());
            ps.setInt(3, income.getCategoryId());
            if (income.getCounterpartyId() != null) ps.setInt(4, income.getCounterpartyId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, income.getUserId());
            ps.setString(6, income.getComment());
            ps.setString(7, income.getFilePath());
            ps.executeUpdate();
        }
    }

    @Override
    public void createExpense(Expense expense) throws Exception {
        String sql = "INSERT INTO expenses (date, amount, category_id, counterparty_id, user_id, comment, file_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(expense.getDate()));
            ps.setBigDecimal(2, expense.getAmount());
            ps.setInt(3, expense.getCategoryId());
            if (expense.getCounterpartyId() != null) ps.setInt(4, expense.getCounterpartyId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, expense.getUserId());
            ps.setString(6, expense.getComment());
            ps.setString(7, expense.getFilePath());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateIncome(Income income) throws Exception {
        String sql = "UPDATE incomes SET amount = ?, category_id = ?, counterparty_id = ?, comment = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, income.getAmount());
            ps.setInt(2, income.getCategoryId());
            if (income.getCounterpartyId() != null) ps.setInt(3, income.getCounterpartyId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, income.getComment());
            ps.setInt(5, income.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateExpense(Expense expense) throws Exception {
        String sql = "UPDATE expenses SET amount = ?, category_id = ?, counterparty_id = ?, comment = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, expense.getAmount());
            ps.setInt(2, expense.getCategoryId());
            if (expense.getCounterpartyId() != null) ps.setInt(3, expense.getCounterpartyId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, expense.getComment());
            ps.setInt(5, expense.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteIncome(int id) throws Exception {
        String sql = "DELETE FROM incomes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteExpense(int id) throws Exception {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<IncomeCategory> getIncomeCategories() throws Exception {
        List<IncomeCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM income_categories WHERE deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                IncomeCategory cat = new IncomeCategory();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setTaxable(rs.getBoolean("taxable"));
                list.add(cat);
            }
        }
        return list;
    }

    @Override
    public void createIncomeCategory(IncomeCategory category) throws Exception {
        String sql = "INSERT INTO income_categories (name, taxable) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isTaxable());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateIncomeCategory(IncomeCategory category) throws Exception {
        String sql = "UPDATE income_categories SET name = ?, taxable = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isTaxable());
            ps.setInt(3, category.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteIncomeCategory(int id) throws Exception {
        String sql = "UPDATE income_categories SET deleted = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<ExpenseCategory> getExpenseCategories() throws Exception {
        List<ExpenseCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM expense_categories WHERE deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ExpenseCategory cat = new ExpenseCategory();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setDeductible(rs.getBoolean("deductible"));
                list.add(cat);
            }
        }
        return list;
    }

    @Override
    public void createExpenseCategory(ExpenseCategory category) throws Exception {
        String sql = "INSERT INTO expense_categories (name, deductible) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isDeductible());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateExpenseCategory(ExpenseCategory category) throws Exception {
        String sql = "UPDATE expense_categories SET name = ?, deductible = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isDeductible());
            ps.setInt(3, category.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteExpenseCategory(int id) throws Exception {
        String sql = "UPDATE expense_categories SET deleted = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Counterparty> getCounterparties() throws Exception {
        List<Counterparty> list = new ArrayList<>();
        String sql = "SELECT * FROM counterparties WHERE deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Counterparty cp = new Counterparty();
                cp.setId(rs.getInt("id"));
                cp.setName(rs.getString("name"));
                cp.setInn(rs.getString("inn"));
                cp.setContactInfo(rs.getString("contact_info"));
                list.add(cp);
            }
        }
        return list;
    }

    @Override
    public void createCounterparty(Counterparty counterparty) throws Exception {
        String sql = "INSERT INTO counterparties (name, inn, contact_info) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterparty.getName());
            ps.setString(2, counterparty.getInn());
            ps.setString(3, counterparty.getContactInfo());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateCounterparty(Counterparty counterparty) throws Exception {
        String sql = "UPDATE counterparties SET name = ?, inn = ?, contact_info = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterparty.getName());
            ps.setString(2, counterparty.getInn());
            ps.setString(3, counterparty.getContactInfo());
            ps.setInt(4, counterparty.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteCounterparty(int id) throws Exception {
        String sql = "UPDATE counterparties SET deleted = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Income mapIncome(ResultSet rs) throws SQLException {
        return new IncomeFactory().createIncome(
                rs.getInt("id"), rs.getDate("date").toLocalDate(), rs.getBigDecimal("amount"),
                rs.getString("comment"), rs.getInt("user_id"), rs.getString("file_path"),
                rs.getInt("category_id"), rs.getObject("counterparty_id", Integer.class));
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        return new ExpenseFactory().createExpense(
                rs.getInt("id"), rs.getDate("date").toLocalDate(), rs.getBigDecimal("amount"),
                rs.getString("comment"), rs.getInt("user_id"), rs.getString("file_path"),
                rs.getInt("category_id"), rs.getObject("counterparty_id", Integer.class));
    }
}