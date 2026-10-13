package com.btpl.physio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.btpl.physio.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderBySlotDateAscSlotStartTimeAsc(Long userId);

    List<Appointment> findByPhysioIdAndSlotDateOrderBySequenceNo(Long physioId, LocalDate date);
}