package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.error.FieldViolation;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FieldValidationException extends RuntimeException {
    private final List<FieldViolation> fieldViolations;
}