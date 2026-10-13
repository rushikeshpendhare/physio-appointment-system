package com.btpl.physio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhysioSummary {

    private Long id;
    private String name;
    private String specialization;
    private Double fees;
    private String clinicAddress;
}