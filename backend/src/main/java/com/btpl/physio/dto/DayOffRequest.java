package com.btpl.physio.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DayOffRequest {

    private LocalDate date;
}