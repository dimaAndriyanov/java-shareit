package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.exception.UnsupportedStateException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BookingValidator {
    public void validateForCreation(BookingDto bookingDto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (bookingDto.getItemId() == null) {
            fieldViolations.add(new FieldViolation("Booking.itemId", "must not be null"));
        }
        if (bookingDto.getStart() == null) {
            fieldViolations.add(new FieldViolation("Booking.start", "must not be null"));
        }
        if (bookingDto.getEnd() == null) {
            fieldViolations.add(new FieldViolation("Booking.end", "must not be null"));
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }

        try {
            start = LocalDateTime.parse(bookingDto.getStart());
        } catch (DateTimeParseException exception) {
            fieldViolations.add(new FieldViolation("Booking.start", "must be of pattern yyyy-MM-ddThh:mm:ss"));
        }
        try {
            end = LocalDateTime.parse(bookingDto.getEnd());
        } catch (DateTimeParseException exception) {
            fieldViolations.add(new FieldViolation("Booking.end", "must be of pattern yyyy-MM-ddThh:mm:ss"));
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }

        if (start.isBefore(now)) {
            fieldViolations.add(new FieldViolation("Booking.start", "must be in future"));
        }
        if (end.isBefore(now)) {
            fieldViolations.add(new FieldViolation("Booking.end", "must be in future"));
        }
        if (end.isBefore(start) || end.equals(start)) {
            fieldViolations.add(new FieldViolation("Booking.end", "must be after start"));
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }

    public BookingState validateBookingState(String bookingState) {
        try {
            return BookingState.valueOf(bookingState);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedStateException("Unknown state: " + bookingState);
        }
    }
}