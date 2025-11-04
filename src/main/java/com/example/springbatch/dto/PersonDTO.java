package com.example.springbatch.dto;

import java.time.LocalDateTime;

public record PersonDTO(
        long id,
        String name,
        String lastName,
        int age,
        String email,
        String city,
        String registered_date,
        String processorDate
) {
}
