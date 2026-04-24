package dao.impl;

import dao.TaxPaymentDAO;
import enums.PaymentStatus;
import models.entities.TaxPayment;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TaxPaymentDAOImpl implements TaxPaymentDAO {

    @Override
    public void create(TaxPayment payment) throws Exception {
        String sql = "INSERT INTO tax_payments (period_start, period_end, tax_type, amount, status, user_id, paid_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(payment.getPeriodStart()));
            ps.setDate(2, Date.valueOf(payment.getPeriodEnd()));
            ps.setString(3, payment.getTaxType());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getStatus().name());
            ps.setInt(6, payment.getUserId());
            if (payment.getPaidDate() != null) {
                ps.setDate(7, Date.valueOf(payment.getPaidDate()));
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public void update(TaxPayment payment) throws Exception {
        if (payment.getId() <= 0) {
            throw new IllegalStateException("Cannot update payment without ID");
        }
        String sql = "UPDATE tax_payments SET amount = ?, status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, payment.getAmount());
            ps.setString(2, payment.getStatus().name());
            ps.setInt(3, payment.getId());
            ps.setInt(4, payment.getUserId());
            ps.executeUpdate();
        }
    }

    @Override
    public TaxPayment findByPeriod(int userId, LocalDate start, LocalDate end, String type) throws Exception {
        String sql = "SELECT * FROM tax_payments WHERE user_id = ? AND period_start = ? AND period_end = ? AND tax_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ps.setString(4, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayment(rs);
                }
            }
        }
        return null;
    }

    private TaxPayment mapPayment(ResultSet rs) throws SQLException {
        TaxPayment p = new TaxPayment();
        p.setId(rs.getInt("id"));
        p.setPeriodStart(rs.getDate("period_start").toLocalDate());
        p.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        p.setTaxType(rs.getString("tax_type"));
        p.setAmount(rs.getBigDecimal("amount"));
        String statusStr = rs.getString("status").toUpperCase();
        p.setStatus(PaymentStatus.valueOf(statusStr));
        p.setUserId(rs.getInt("user_id"));
        java.sql.Date pd = rs.getDate("paid_date");
        if (pd != null) {
            p.setPaidDate(pd.toLocalDate());
        }
        return p;
    }
}