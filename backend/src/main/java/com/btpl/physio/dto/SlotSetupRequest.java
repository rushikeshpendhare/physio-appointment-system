package com.btpl.physio.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlotSetupRequest {

    private LocalDate date;
    private LocalTime startTime;     // e.g. 10:00
    private LocalTime endTime;       // e.g. 13:00
    private int durationMinutes;     // e.g. 30
}