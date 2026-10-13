package com.btpl.physio.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReorderRequest {

    private LocalDate date;
    private List<Long> appointmentIds;   // new order, first id = sequence 1
}