package com.btpl.physio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.btpl.physio.dto.BookingRequest;
import com.btpl.physio.dto.PhysioSummary;
import com.btpl.physio.model.Appointment;
import com.btpl.physio.model.Role;
import com.btpl.physio.model.SlotStatus;
import com.btpl.physio.repository.SlotRepository;
import com.btpl.physio.repository.UserRepository;
import com.btpl.physio.service.BookingService;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final BookingService bookingService;

    public PatientController(UserRepository userRepository,
                             SlotRepository slotRepository,
                             BookingService bookingService) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/physios")
    public List<PhysioSummary> listPhysios() {
        return userRepository.findByRole(Role.PHYSIO).stream()
                .filter(u -> u.isEmailVerified())
                .limit(3)
                .map(u -> new PhysioSummary(u.getId(), u.getName(),
                        u.getSpecialization(), u.getFees(), u.getClinicAddress()))
                .toList();
    }

    @GetMapping("/physios/{physioId}/slots")
    public List<Map<String, Object>> availableSlots(
            @PathVariable Long physioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return slotRepository.findByPhysioIdAndDateOrderByStartTime(physioId, date).stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "date", s.getDate().toString(),
                        "startTime", s.getStartTime().toString(),
                        "endTime", s.getEndTime().toString()))
                .toList();
    }

    @PostMapping("/book")
    public ResponseEntity<?> book(Authentication auth, @RequestBody BookingRequest req) {
        try {
            Appointment a = bookingService.book(auth.getName(), req);
            return ResponseEntity.ok(toMap(a));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments")
    public List<Map<String, Object>> myAppointments(Authentication auth) {
        return bookingService.myAppointments(auth.getName()).stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(Appointment a) {
        return Map.of(
                "id", a.getId(),
                "date", a.getSlot().getDate().toString(),
                "startTime", a.getSlot().getStartTime().toString(),
                "endTime", a.getSlot().getEndTime().toString(),
                "physioName", a.getPhysio().getName(),
                "status", a.getStatus().name(),
                "paymentStatus", a.getPaymentStatus(),
                "amount", a.getAmount());
    }
}