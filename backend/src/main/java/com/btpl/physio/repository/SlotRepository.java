package com.btpl.physio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.btpl.physio.model.Slot;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByPhysioIdAndDateOrderByStartTime(Long physioId, LocalDate date);
}