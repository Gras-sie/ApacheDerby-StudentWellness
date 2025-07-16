package com.wellness.service;

import com.wellness.dao.AppointmentDAO;
import com.wellness.dao.CounselorDAO;
import com.wellness.exception.ServiceException;
import com.wellness.model.Appointment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the AppointmentService interface.
 * Handles business logic for appointment operations.
 */
public class AppointmentServiceImpl extends BaseServiceImpl<Appointment, Integer> implements AppointmentService {
    
    private static final int MAX_APPOINTMENT_DURATION_HOURS = 4;
    private static final int MIN_APPOINTMENT_DURATION_MINUTES = 15;
    private static final int MAX_APPOINTMENTS_PER_DAY = 2;
    
    private final AppointmentDAO appointmentDAO;
    private final CounselorDAO counselorDAO;
    
    /**
     * Constructs a new AppointmentServiceImpl.
     *
     * @param appointmentDAO The appointment data access object
     * @param counselorDAO The counselor data access object
     */
    public AppointmentServiceImpl(AppointmentDAO appointmentDAO, CounselorDAO counselorDAO) {
        super(appointmentDAO, "appointment");
        this.appointmentDAO = appointmentDAO;
        this.counselorDAO = counselorDAO;
    }
    
    @Override
    protected void validateForSave(Appointment appointment) throws ServiceException {
        super.validateForSave(appointment);
        
        // Validate required fields
        if (appointment.getCounselorId() == null) {
            throw ServiceException.validationError("Counselor ID is required");
        }
        if (appointment.getStudentId() == null) {
            throw ServiceException.validationError("Student ID is required");
        }
        if (appointment.getStartTime() == null) {
            throw ServiceException.validationError("Start time is required");
        }
        if (appointment.getEndTime() == null) {
            throw ServiceException.validationError("End time is required");
        }
        
        // Validate time range
        validateAppointmentTimeRange(appointment);
        
        // Check if counselor exists
        if (!counselorDAO.existsById(appointment.getCounselorId())) {
            throw ServiceException.validationError("Counselor not found");
        }
        
        // Check for scheduling conflicts
        if (!isTimeSlotAvailable(
            appointment.getCounselorId(),
            appointment.getStartTime(),
            appointment.getEndTime(),
            appointment.getId()
        )) {
            throw ServiceException.conflict("The requested time slot is not available");
        }
        
        // Check student appointment limits
        checkStudentAppointmentLimit(appointment);
    }
    
    private void validateAppointmentTimeRange(Appointment appointment) throws ServiceException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = appointment.getStartTime();
        LocalDateTime end = appointment.getEndTime();
        
        // Check if start time is in the future
        if (start.isBefore(now)) {
            throw ServiceException.validationError("Start time cannot be in the past");
        }
        
        // Check if end time is after start time
        if (!end.isAfter(start)) {
            throw ServiceException.validationError("End time must be after start time");
        }
        
        // Check minimum duration
        Duration duration = Duration.between(start, end);
        if (duration.toMinutes() < MIN_APPOINTMENT_DURATION_MINUTES) {
            throw ServiceException.validationError(
                String.format("Minimum appointment duration is %d minutes", MIN_APPOINTMENT_DURATION_MINUTES)
            );
        }
        
        // Check maximum duration
        if (duration.toHours() > MAX_APPOINTMENT_DURATION_HOURS) {
            throw ServiceException.validationError(
                String.format("Maximum appointment duration is %d hours", MAX_APPOINTMENT_DURATION_HOURS)
            );
        }
    }
    
    private void checkStudentAppointmentLimit(Appointment appointment) throws ServiceException {
        LocalDateTime startOfDay = appointment.getStartTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        long existingAppointments = appointmentDAO.countByStudentAndDateRange(
            appointment.getStudentId(),
            startOfDay,
            endOfDay
        );
        
        // If this is an update, don't count the current appointment
        if (appointment.getId() != null) {
            existingAppointments--;
        }
        
        if (existingAppointments >= MAX_APPOINTMENTS_PER_DAY) {
            throw ServiceException.validationError(
                String.format("Maximum of %d appointments per day allowed", MAX_APPOINTMENTS_PER_DAY)
            );
        }
    }
    
    @Override
    public List<Appointment> findByStudentId(Integer studentId) throws ServiceException {
        try {
            if (studentId == null) {
                throw ServiceException.invalidInput("Student ID cannot be null");
            }
            return appointmentDAO.findByStudentId(studentId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding appointments by student ID: " + studentId, e);
        }
    }
    
    @Override
    public List<Appointment> findByCounselorId(Integer counselorId) throws ServiceException {
        try {
            if (counselorId == null) {
                throw ServiceException.invalidInput("Counselor ID cannot be null");
            }
            return appointmentDAO.findByCounselorId(counselorId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding appointments by counselor ID: " + counselorId, e);
        }
    }
    
    @Override
    public List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        try {
            if (start == null || end == null) {
                throw ServiceException.invalidInput("Start and end times cannot be null");
            }
            if (end.isBefore(start)) {
                throw ServiceException.invalidInput("End time must be after start time");
            }
            return appointmentDAO.findByDateRange(start, end);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding appointments by date range", e);
        }
    }
    
    @Override
    public List<Appointment> findByStatus(Appointment.Status status) throws ServiceException {
        try {
            if (status == null) {
                throw ServiceException.invalidInput("Status cannot be null");
            }
            return appointmentDAO.findByStatus(status);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding appointments by status: " + status, e);
        }
    }
    
    @Override
    public boolean isTimeSlotAvailable(Integer counselorId, LocalDateTime startTime, LocalDateTime endTime, 
                                     Integer excludeAppointmentId) throws ServiceException {
        try {
            if (counselorId == null || startTime == null || endTime == null) {
                throw ServiceException.invalidInput("Counselor ID, start time, and end time are required");
            }
            
            if (endTime.isBefore(startTime)) {
                throw ServiceException.invalidInput("End time must be after start time");
            }
            
            List<Appointment> conflicts = appointmentDAO.findConflictingAppointments(
                counselorId, startTime, endTime, excludeAppointmentId);
                
            return conflicts.isEmpty();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error checking time slot availability", e);
        }
    }
    
    @Override
    public Appointment cancelAppointment(Integer appointmentId, String reason) throws ServiceException {
        try {
            if (appointmentId == null) {
                throw ServiceException.invalidInput("Appointment ID cannot be null");
            }
            
            Appointment appointment = getByIdOrThrow(appointmentId);
            
            // Check if appointment can be cancelled
            if (appointment.getStatus() == Appointment.Status.COMPLETED) {
                throw ServiceException.validationError("Cannot cancel a completed appointment");
            }
            if (appointment.getStatus() == Appointment.Status.CANCELLED) {
                throw ServiceException.validationError("Appointment is already cancelled");
            }
            
            // Update status and save
            appointment.cancel(reason);
            return save(appointment);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error cancelling appointment: " + appointmentId, e);
        }
    }
    
    @Override
    public Appointment completeAppointment(Integer appointmentId) throws ServiceException {
        try {
            if (appointmentId == null) {
                throw ServiceException.invalidInput("Appointment ID cannot be null");
            }
            
            Appointment appointment = getByIdOrThrow(appointmentId);
            
            // Check if appointment can be completed
            if (appointment.getStatus() == Appointment.Status.CANCELLED) {
                throw ServiceException.validationError("Cannot complete a cancelled appointment");
            }
            if (appointment.getStatus() == Appointment.Status.COMPLETED) {
                throw ServiceException.validationError("Appointment is already completed");
            }
            
            // Update status and save
            appointment.markAsCompleted();
            return save(appointment);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error completing appointment: " + appointmentId, e);
        }
    }
    
    @Override
    public long countByStudentAndDateRange(Integer studentId, LocalDateTime start, LocalDateTime end) 
            throws ServiceException {
        try {
            if (studentId == null || start == null || end == null) {
                throw ServiceException.invalidInput("Student ID, start time, and end time are required");
            }
            if (end.isBefore(start)) {
                throw ServiceException.invalidInput("End time must be after start time");
            }
            
            return appointmentDAO.countByStudentAndDateRange(studentId, start, end);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error counting student appointments", e);
        }
    }
}
