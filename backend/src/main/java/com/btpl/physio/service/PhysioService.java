package com.btpl.physio.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.btpl.physio.model.Appointment;
import com.btpl.physio.model.Slot;
import com.btpl.physio.model.SlotStatus;
import com.btpl.physio.model.User;
import com.btpl.physio.repository.AppointmentRepository;
import com.btpl.physio.repository.SlotRepository;
import com.btpl.physio.repository.UserRepository;

@Service
public class PhysioService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

    public PhysioService(AppointmentRepository appointmentRepository,
                         UserRepository userRepository,
                         SlotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
    }

    private User getPhysio(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Physio not found"));
    }

    public List<Appointment> dailyAppointments(String email, LocalDate date) {
        User physio = getPhysio(email);
        return appointmentRepository
                .findByPhysioIdAndSlotDateOrderBySequenceNo(physio.getId(), date);
    }

    @Transactional
    public void reorder(String email, LocalDate date, List<Long> ids) {
        User physio = getPhysio(email);
        List<Appointment> list = appointmentRepository
                .findByPhysioIdAndSlotDateOrderBySequenceNo(physio.getId(), date);

        if (ids == null || ids.size() != list.size() || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Send every appointment id of that day exactly once");
        }

        Map<Long, Appointment> byId = list.stream()
                .collect(Collectors.toMap(Appointment::getId, Function.identity()));

        int seq = 1;
        for (Long id : ids) {
            Appointment a = byId.get(id);
            if (a == null) {
                throw new IllegalArgumentException("Invalid appointment id: " + id);
            }
            a.setSequenceNo(seq++);
        }
        appointmentRepository.saveAll(list);
    }

    @Transactional
    public int markDayOff(String email, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        User physio = getPhysio(email);
        List<Slot> slots = slotRepository
                .findByPhysioIdAndDateOrderByStartTime(physio.getId(), date);

        int changed = 0;
        for (Slot s : slots) {
            if (s.getStatus() == SlotStatus.AVAILABLE) {   // BOOKED slots stay as they are
                s.setStatus(SlotStatus.OFF);
                changed++;
            }
        }
        slotRepository.saveAll(slots);
        return changed;
    }

    @Transactional
    public void updateFees(String email, Double fees) {
        if (fees == null || fees <= 0) {
            throw new IllegalArgumentException("Fees must be greater than 0");
        }
        User physio = getPhysio(email);
        physio.setFees(fees);
        userRepository.save(physio);
    }
}