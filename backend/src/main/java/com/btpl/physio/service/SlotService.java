package com.btpl.physio.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.btpl.physio.dto.SlotSetupRequest;
import com.btpl.physio.model.Slot;
import com.btpl.physio.model.SlotStatus;
import com.btpl.physio.model.User;
import com.btpl.physio.repository.SlotRepository;
import com.btpl.physio.repository.UserRepository;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public SlotService(SlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<Slot> createSlots(String physioEmail, SlotSetupRequest req) {
        if (req.getDate() == null || req.getStartTime() == null || req.getEndTime() == null
                || req.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Date, start time, end time and duration are required");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        User physio = userRepository.findByEmail(physioEmail)
                .orElseThrow(() -> new IllegalArgumentException("Physio not found"));

        // Existing slots of that day
        List<Slot> existing = slotRepository
                .findByPhysioIdAndDateOrderByStartTime(physio.getId(), req.getDate());

        // Keep BOOKED slots, delete the rest (they will be regenerated)
        List<Slot> booked = new ArrayList<>();
        List<Slot> toDelete = new ArrayList<>();
        for (Slot s : existing) {
            if (s.getStatus() == SlotStatus.BOOKED) {
                booked.add(s);
            } else {
                toDelete.add(s);
            }
        }
        slotRepository.deleteAll(toDelete);

        // Generate new slots
        List<Slot> created = new ArrayList<>();
        LocalTime start = req.getStartTime();
        int duration = req.getDurationMinutes();

        while (true) {
            LocalTime end = start.plusMinutes(duration);
            if (!end.isAfter(start) || end.isAfter(req.getEndTime())) {
                break;
            }

            final LocalTime slotStart = start;
            final LocalTime slotEnd = end;
            boolean overlapsBooked = booked.stream().anyMatch(b ->
                    b.getStartTime().isBefore(slotEnd) && slotStart.isBefore(b.getEndTime()));

            if (!overlapsBooked) {
                Slot slot = new Slot();
                slot.setPhysio(physio);
                slot.setDate(req.getDate());
                slot.setStartTime(slotStart);
                slot.setEndTime(slotEnd);
                slot.setStatus(SlotStatus.AVAILABLE);
                created.add(slot);
            }
            start = end;
        }

        return slotRepository.saveAll(created);
    }
}