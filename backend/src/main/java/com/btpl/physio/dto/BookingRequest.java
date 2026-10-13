package com.btpl.physio.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    private Long slotId;
    private String paymentMethod;   // mock: "CARD" or "UPI"
}