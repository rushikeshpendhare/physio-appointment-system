package com.btpl.physio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.btpl.physio.dto.DayOffRequest;
import com.btpl.physio.dto.FeesRequest;
import com.btpl.physio.dto.ReorderRequest;
import com.btpl.physio.dto.SlotSetupRequest;
import com.btpl.physio.model.Appointment;
import com.btpl.physio.model.Slot;
import com.btpl.physio.service.PhysioService;
import com.btpl.physio.service.SlotService;

@RestController
@RequestMapping("/api/physio")
public class PhysioController {

    private final SlotService slotService;
    private final PhysioService physioService;

    public PhysioController(SlotService slotService, PhysioService physioService) {
        this.slotService = slotService;
        this.physioService = physioService;
    }

    @PostMapping("/slots")
    public ResponseEntity<?> createSlots(Authentication auth,
                                         @RequestBody SlotSetupRequest req) {
        try {
            List<Slot> slots = slotService.createSlots(auth.getName(), req);
            List<Map<String, Object>> result = slots.stream()
                    .map(s -> Map.<String, Object>of(
                            "id", s.getId(),
                            "date", s.getDate().toString(),
                            "startTime", s.getStartTime().toString(),
                            "endTime", s.getEndTime().toString(),
                            "status", s.getStatus().name()))
                    .toList();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> dailyAppointments(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Map<String, Object>> result = physioService
                    .dailyAppointments(auth.getName(), date).stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/reorder")
    public ResponseEntity<?> reorder(Authentication auth, @RequestBody ReorderRequest req) {
        try {
            physioService.reorder(auth.getName(), req.getDate(), req.getAppointmentIds());
            return ResponseEntity.ok(Map.of("message", "Order updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/day-off")
    public ResponseEntity<?> dayOff(Authentication auth, @RequestBody DayOffRequest req) {
        try {
            int changed = physioService.markDayOff(auth.getName(), req.getDate());
            return ResponseEntity.ok(Map.of("message", "Day marked off",
                    "slotsClosed", changed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/fees")
    public ResponseEntity<?> updateFees(Authentication auth, @RequestBody FeesRequest req) {
        try {
            physioService.updateFees(auth.getName(), req.getFees());
            return ResponseEntity.ok(Map.of("message", "Fees updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toMap(Appointment a) {
        return Map.of(
                "id", a.getId(),
                "sequenceNo", a.getSequenceNo(),
                "date", a.getSlot().getDate().toString(),
                "startTime", a.getSlot().getStartTime().toString(),
                "endTime", a.getSlot().getEndTime().toString(),
                "patientName", a.getUser().getName(),
                "patientContact", String.valueOf(a.getUser().getContactNumber()),
                "status", a.getStatus().name());
    }
}