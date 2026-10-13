package com.btpl.physio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.btpl.physio.dto.BookingRequest;
import com.btpl.physio.model.Appointment;
import com.btpl.physio.model.AppointmentStatus;
import com.btpl.physio.model.Slot;
import com.btpl.physio.model.SlotStatus;
import com.btpl.physio.model.User;
import com.btpl.physio.repository.AppointmentRepository;
import com.btpl.physio.repository.SlotRepository;
import com.btpl.physio.repository.UserRepository;

@Service
public class BookingService {

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public BookingService(SlotRepository slotRepository,
                          UserRepository userRepository,
                          AppointmentRepository appointmentRepository,
                          EmailService emailService) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Appointment book(String userEmail, BookingRequest req) {
        if (req.getSlotId() == null) {
            throw new IllegalArgumentException("Slot is required");
        }

        User patient = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Slot slot = slotRepository.findById(req.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException("Slot is no longer available");
        }

        User physio = slot.getPhysio();

        // Mock payment: always succeeds
        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        // Next sequence number for that physio on that day
        int nextSeq = appointmentRepository
                .findByPhysioIdAndSlotDateOrderBySequenceNo(physio.getId(), slot.getDate())
                .size() + 1;

        Appointment appt = new Appointment();
        appt.setUser(patient);
        appt.setPhysio(physio);
        appt.setSlot(slot);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.setPaymentStatus("PAID");
        appt.setAmount(physio.getFees());
        appt.setSequenceNo(nextSeq);
        appt = appointmentRepository.save(appt);

        String when = slot.getDate() + " " + slot.getStartTime() + " - " + slot.getEndTime();

        emailService.sendBookingConfirmation(patient.getEmail(),
                "Appointment confirmed",
                "Hi " + patient.getName() + ",\n\nYour appointment with " + physio.getName()
                        + " is confirmed.\nWhen: " + when
                        + "\nClinic: " + physio.getClinicAddress()
                        + "\nAmount paid: " + physio.getFees());

        emailService.sendBookingConfirmation(physio.getEmail(),
                "New appointment booked",
                "Hi " + physio.getName() + ",\n\nNew appointment booked by " + patient.getName()
                        + ".\nWhen: " + when
                        + "\nPatient contact: " + patient.getContactNumber());

        return appt;
    }

    public List<Appointment> myAppointments(String userEmail) {
        User patient = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return appointmentRepository
                .findByUserIdOrderBySlotDateAscSlotStartTimeAsc(patient.getId());
    }
}