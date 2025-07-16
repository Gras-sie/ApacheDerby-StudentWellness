package com.wellness.dao;

import com.wellness.model.Appointment;
import com.wellness.model.Appointment.Status;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the AppointmentDAO interface for Apache Derby database.
 */
public class AppointmentDAOImpl extends BaseDAOImpl<Appointment, Integer> implements AppointmentDAO {
    
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAOImpl.class.getName());
    
    // SQL Queries
    private static final String TABLE_NAME = "appointments";
    private static final String FIND_BY_STUDENT_ID = "SELECT * FROM " + TABLE_NAME + " WHERE student_id = ? ORDER BY start_time";
    private static final String FIND_BY_COUNSELOR_ID = "SELECT * FROM " + TABLE_NAME + " WHERE counselor_id = ? ORDER BY start_time";
    private static final String FIND_BY_DATE_RANGE = "SELECT * FROM " + TABLE_NAME + " WHERE start_time >= ? AND end_time <= ?";
    private static final String FIND_BY_STATUS = "SELECT * FROM " + TABLE_NAME + " WHERE status = ?";
    
    public AppointmentDAOImpl(Connection connection) {
        super(connection, TABLE_NAME);
    }
    
    @Override
    protected Appointment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setStudentId(rs.getInt("student_id"));
        appointment.setCounselorId(rs.getInt("counselor_id"));
        appointment.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        appointment.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        appointment.setStatus(Status.valueOf(rs.getString("status")));
        appointment.setNotes(rs.getString("notes"));
        
        // Use reflection to set created_date and updated_date since they're protected
        try {
            appointment.getClass().getMethod("setCreatedDate", java.time.LocalDateTime.class)
                .invoke(appointment, rs.getTimestamp("created_date").toLocalDateTime());
            appointment.getClass().getMethod("setUpdatedDate", java.time.LocalDateTime.class)
                .invoke(appointment, rs.getTimestamp("updated_date").toLocalDateTime());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set created/updated dates", e);
        }
        
        return appointment;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Appointment entity) throws SQLException {
        ps.setInt(1, entity.getStudentId());
        ps.setInt(2, entity.getCounselorId());
        ps.setTimestamp(3, Timestamp.valueOf(entity.getStartTime()));
        ps.setTimestamp(4, Timestamp.valueOf(entity.getEndTime()));
        ps.setString(5, entity.getStatus().name());
        ps.setString(6, entity.getNotes());
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        try {
            entity.getClass().getMethod("setCreatedDate", java.time.LocalDateTime.class).invoke(entity, now);
            entity.getClass().getMethod("setUpdatedDate", java.time.LocalDateTime.class).invoke(entity, now);
            
            // Set the timestamp parameters for the prepared statement
            ps.setTimestamp(7, Timestamp.valueOf(now)); // created_date
            ps.setTimestamp(8, Timestamp.valueOf(now)); // updated_date
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set created/updated dates", e);
            // Set default timestamps in case reflection fails
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.setTimestamp(8, Timestamp.valueOf(now));
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Appointment entity) throws SQLException {
        // Set all fields except ID
        ps.setInt(1, entity.getStudentId());
        ps.setInt(2, entity.getCounselorId());
        ps.setTimestamp(3, Timestamp.valueOf(entity.getStartTime()));
        ps.setTimestamp(4, Timestamp.valueOf(entity.getEndTime()));
        ps.setString(5, entity.getStatus().name());
        ps.setString(6, entity.getNotes());
        
        // Set updated timestamp
        LocalDateTime now = LocalDateTime.now();
        try {
            entity.getClass().getMethod("setUpdatedDate", java.time.LocalDateTime.class).invoke(entity, now);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set updated date", e);
        }
        
        // Set the updated_date parameter
        ps.setTimestamp(7, Timestamp.valueOf(now));
        
        // Set ID for WHERE clause
        ps.setInt(8, entity.getId());
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO " + tableName + " (student_id, counselor_id, start_time, end_time, status, notes, created_date, updated_date) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE " + tableName + " SET student_id = ?, counselor_id = ?, start_time = ?, " +
               "end_time = ?, status = ?, notes = ?, updated_date = ? WHERE id = ?";
    }
    
    @Override
    public List<Appointment> findByStudentId(Integer studentId) {
        return executeQueryList(FIND_BY_STUDENT_ID, studentId);
    }
    
    @Override
    public List<Appointment> findByCounselorId(Integer counselorId) {
        return executeQueryList(FIND_BY_COUNSELOR_ID, counselorId);
    }
    
    @Override
    public List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return executeQueryList(FIND_BY_DATE_RANGE, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    @Override
    public List<Appointment> findByStatus(Appointment.Status status) {
        return executeQueryList(FIND_BY_STATUS, status.name());
    }
    
    @Override
    public List<Appointment> findConflictingAppointments(Integer counselorId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeAppointmentId) {
        String query = "SELECT * FROM " + tableName + " WHERE counselor_id = ? AND id != ? AND " +
                      "((start_time < ? AND end_time > ?) OR " +  // Overlapping start
                      " (start_time < ? AND end_time > ?) OR " +   // Overlapping end
                      " (start_time >= ? AND end_time <= ?))";     // Contained within
        
        return executeQueryList(query, 
            counselorId,
            excludeAppointmentId != null ? excludeAppointmentId : -1,  // Use -1 as a placeholder for null
            endTime, startTime,  // For first condition
            endTime, startTime,  // For second condition
            startTime, endTime   // For third condition
        );
    }
    
    @Override
    public boolean isTimeSlotAvailable(Integer counselorId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeAppointmentId) {
        return findConflictingAppointments(counselorId, startTime, endTime, excludeAppointmentId).isEmpty();
    }
    
    @Override
    public long countByStudentAndDateRange(Integer studentId, LocalDateTime start, LocalDateTime end) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE student_id = ? AND start_time >= ? AND end_time <= ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, studentId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting appointments by student and date range", e);
            return 0;
        }
    }
}
