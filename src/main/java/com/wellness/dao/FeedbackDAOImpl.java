package com.wellness.dao;

import com.wellness.model.Feedback;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of FeedbackDAO providing database operations for Feedback entities.
 */
public class FeedbackDAOImpl extends BaseDAOImpl<Feedback, Integer> implements FeedbackDAO {
    
    private static final String TABLE_NAME = "feedback";
    private static final Logger LOGGER = Logger.getLogger(FeedbackDAOImpl.class.getName());
    
    public FeedbackDAOImpl(Connection connection) {
        super(connection, TABLE_NAME);
    }
    
    @Override
    protected Feedback mapResultSetToEntity(ResultSet rs) throws SQLException {
        if (rs == null) {
            return null;
        }
        
        Feedback feedback = new Feedback();
        feedback.setId(rs.getInt("id"));
        feedback.setAppointmentId(rs.getInt("appointment_id"));
        feedback.setStudentId(rs.getInt("student_id"));
        feedback.setCounselorId(rs.getInt("counselor_id"));
        feedback.setRating(rs.getInt("rating"));
        feedback.setComments(rs.getString("comments"));
        feedback.setAnonymous(rs.getBoolean("is_anonymous"));
        
        // Handle timestamps using reflection to access protected methods
        try {
            Timestamp created = rs.getTimestamp("created_date");
            Timestamp updated = rs.getTimestamp("updated_date");
            
            if (created != null) {
                java.lang.reflect.Method setCreatedDate = Feedback.class.getDeclaredMethod("setCreatedDate", java.time.LocalDateTime.class);
                setCreatedDate.setAccessible(true);
                setCreatedDate.invoke(feedback, created.toLocalDateTime());
            }
            
            if (updated != null) {
                java.lang.reflect.Method setUpdatedDate = Feedback.class.getDeclaredMethod("setUpdatedDate", java.time.LocalDateTime.class);
                setUpdatedDate.setAccessible(true);
                setUpdatedDate.invoke(feedback, updated.toLocalDateTime());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting dates on Feedback entity", e);
        }
        
        return feedback;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Feedback entity) throws SQLException {
        if (ps == null || entity == null) {
            return;
        }
        
        int index = 1;
        ps.setInt(index++, entity.getAppointmentId());
        ps.setInt(index++, entity.getStudentId());
        ps.setInt(index++, entity.getCounselorId());
        ps.setInt(index++, entity.getRating());
        ps.setString(index++, entity.getComments());
        ps.setBoolean(index++, entity.isAnonymous());
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        ps.setTimestamp(index++, Timestamp.valueOf(now));
        ps.setTimestamp(index, Timestamp.valueOf(now));
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Feedback entity) throws SQLException {
        if (ps == null || entity == null) {
            return;
        }
        
        int index = 1;
        ps.setInt(index++, entity.getAppointmentId());
        ps.setInt(index++, entity.getStudentId());
        ps.setInt(index++, entity.getCounselorId());
        ps.setInt(index++, entity.getRating());
        ps.setString(index++, entity.getComments());
        ps.setBoolean(index++, entity.isAnonymous());
        
        // Set updated timestamp
        ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
        
        // WHERE id = ?
        ps.setInt(index, entity.getId());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO " + tableName + " (appointment_id, student_id, counselor_id, rating, comments, is_anonymous, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE " + tableName + " SET appointment_id = ?, student_id = ?, counselor_id = ?, rating = ?, comments = ?, is_anonymous = ?, updated_date = ? WHERE id = ?";
    }

    @Override
    public Feedback save(Feedback entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Feedback entity cannot be null");
        }
        
        try {
            if (entity.getId() == null) {
                // Create new feedback
                String sql = getInsertSQL();
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setInsertParameters(ps, entity);
                    int affectedRows = ps.executeUpdate();
                    
                    if (affectedRows == 0) {
                        throw new SQLException("Creating feedback failed, no rows affected.");
                    }
                    
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            entity.setId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Creating feedback failed, no ID obtained.");
                        }
                    }
                }
            } else {
                // Update existing feedback
                String sql = getUpdateSQL();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    setUpdateParameters(ps, entity);
                    int affectedRows = ps.executeUpdate();
                    
                    if (affectedRows == 0) {
                        throw new SQLException("Updating feedback failed, no rows affected.");
                    }
                }
            }
            
            return entity;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving feedback", e);
            return null;
        }
    }

    @Override
    public Optional<Feedback> findById(Integer id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding feedback by id: " + id, e);
        }
        return Optional.empty();
    }
    
    @Override
    public List<Feedback> findAll() {
        String sql = "SELECT * FROM " + tableName + " ORDER BY created_date DESC";
        return executeQuery(sql, ps -> {});
    }
    
    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if entity exists: " + id, e);
        }
        return false;
    }
    
    @Override
    public boolean delete(Feedback entity) {
        if (entity != null && entity.getId() != null) {
            return deleteById(entity.getId());
        }
        return false;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting feedback with id: " + id, e);
            return false;
        }
    }
    
    @Override
    public double getAverageRatingForCounselor(Integer counselorId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM " + tableName + " WHERE counselor_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, counselorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting average rating for counselor: " + counselorId, e);
        }
        return 0.0;
    }
    
    @Override
    public long countByCounselorId(Integer counselorId) {
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE counselor_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, counselorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("count");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting feedback for counselor: " + counselorId, e);
        }
        return 0;
    }
    
    @Override
    public long countByStudentId(Integer studentId) {
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("count");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting feedback for student " + studentId, e);
        }
        return 0;
    }
    
    @Override
    public boolean existsByAppointmentId(Integer appointmentId) {
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE appointment_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if feedback exists for appointment " + appointmentId, e);
        }
        return false;
    }
    
    @Override
    public int[] getRatingDistribution(Integer counselorId) {
        int[] distribution = new int[5]; // Index 0 = 1-star, 1 = 2-star, etc.
        String sql = "SELECT rating, COUNT(*) as count FROM " + tableName + 
                    " WHERE counselor_id = ? GROUP BY rating";
                    
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, counselorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rating = rs.getInt("rating");
                    if (rating >= 1 && rating <= 5) {
                        distribution[rating - 1] = rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting rating distribution for counselor " + counselorId, e);
        }
        
        return distribution;
    }
    
    @Override
    public List<Feedback> findAnonymousFeedback() {
        String sql = "SELECT * FROM " + tableName + " WHERE is_anonymous = true ORDER BY created_date DESC";
        return executeQuery(sql, ps -> {});
    }
    
    @Override
    public List<Feedback> findNonAnonymousFeedback() {
        String sql = "SELECT * FROM " + tableName + " WHERE is_anonymous = false ORDER BY created_date DESC";
        return executeQuery(sql, ps -> {});
    }

    // Implementation of FeedbackDAO specific methods
    
    @Override
    public List<Feedback> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM " + tableName + " WHERE student_id = ? ORDER BY created_date DESC";
        return executeQuery(sql, ps -> ps.setInt(1, studentId));
    }

    @Override
    public List<Feedback> findByCounselorId(Integer counselorId) {
        String sql = "SELECT * FROM " + tableName + " WHERE counselor_id = ? ORDER BY created_date DESC";
        return executeQuery(sql, ps -> ps.setInt(1, counselorId));
    }

    @Override
    public List<Feedback> findByAppointmentId(Integer appointmentId) {
        String sql = "SELECT * FROM " + tableName + " WHERE appointment_id = ?";
        return executeQuery(sql, ps -> ps.setInt(1, appointmentId));
    }

    @Override
    public List<Feedback> findByRatingRange(int minRating, int maxRating) {
        String sql = "SELECT * FROM " + tableName + " WHERE rating BETWEEN ? AND ? ORDER BY rating DESC, created_date DESC";
        return executeQuery(sql, ps -> {
            ps.setInt(1, minRating);
            ps.setInt(2, maxRating);
        });
    }



    @Override
    public double getAverageRatingByCounselorId(Integer counselorId) {
        String sql = "SELECT AVG(rating) FROM " + tableName + " WHERE counselor_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, counselorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating average rating for counselor: " + counselorId, e);
        }
        return 0.0;
    }
    
    @Override
    public int getFeedbackCountByCounselorId(Integer counselorId) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE counselor_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, counselorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting feedback for counselor: " + counselorId, e);
        }
        return 0;
    }
    
    @Override
    public List<Feedback> getFeedbackByStudentId(Integer studentId) {
        return findByStudentId(studentId);
    }
    
    @Override
    public List<Feedback> getFeedbackByAppointmentId(Integer appointmentId) {
        return findByAppointmentId(appointmentId);
    }
    
    @Override
    public List<Feedback> getFeedbackByRatingRange(int minRating, int maxRating) {
        return findByRatingRange(minRating, maxRating);
    }
    
    @Override
    public List<Feedback> searchFeedback(String searchTerm) {
        String sql = "SELECT * FROM " + tableName + " " +
                    "WHERE comments LIKE ? " +
                    "OR id IN (SELECT f.id FROM feedback f " +
                    "JOIN students s ON f.student_id = s.id " +
                    "WHERE CONCAT(s.first_name, ' ', s.last_name) LIKE ?) " +
                    "ORDER BY created_date DESC";
        
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, ps -> {
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
        });
    }
    
    private List<Feedback> executeQuery(String sql, ParameterSetter parameterSetter) {
        List<Feedback> feedbacks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            parameterSetter.setParameters(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + sql, e);
        }
        return feedbacks;
    }
    
    @FunctionalInterface
    private interface ParameterSetter {
        void setParameters(PreparedStatement ps) throws SQLException;
    }
}
