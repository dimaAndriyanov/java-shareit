package ru.practicum.shareit.error;

import lombok.Data;

@Data
public class FieldViolation {
    private final String fieldName;
    private final String message;
}