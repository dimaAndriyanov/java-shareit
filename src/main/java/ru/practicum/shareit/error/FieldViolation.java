package ru.practicum.shareit.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class FieldViolation {
    private final String fieldName;
    private final String message;
}