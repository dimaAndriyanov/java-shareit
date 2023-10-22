package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.exception.UnsupportedStateException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingValidatorTest {

    @Test
    void shouldThrowFieldValidationExceptionWhenValidateForCreationWithDifferentMisplacedParams() {
        BookingDto bookingWithNullFields = new BookingDto(null, null, null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> BookingValidator.validateForCreation(bookingWithNullFields));
        assertEquals(3, fieldValidationException.getFieldViolations().size());
        assertEquals(new HashSet<>(fieldValidationException.getFieldViolations()),
                Set.of(new FieldViolation("Booking.itemId", "must not be null"),
                        new FieldViolation("Booking.start", "must not be null"),
                        new FieldViolation("Booking.end", "must not be null")));

        BookingDto bookingWithWrongTimePattern = new BookingDto(1L, "22.08.2025, 12:05:15", "now");
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> BookingValidator.validateForCreation(bookingWithWrongTimePattern));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertEquals(new HashSet<>(fieldValidationException.getFieldViolations()),
                Set.of(new FieldViolation("Booking.start", "must be of pattern yyyy-MM-ddThh:mm:ss"),
                        new FieldViolation("Booking.end", "must be of pattern yyyy-MM-ddThh:mm:ss")));

        BookingDto bookingWithEndBeforeStartAndStartInPast =
                new BookingDto(1L, "2000-01-01T12:00:00", "1995-01-01T12:00:00");
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> BookingValidator.validateForCreation(bookingWithEndBeforeStartAndStartInPast));
        assertEquals(3, fieldValidationException.getFieldViolations().size());
        assertEquals(new HashSet<>(fieldValidationException.getFieldViolations()),
                Set.of(new FieldViolation("Booking.start", "must be in future"),
                        new FieldViolation("Booking.end", "must be in future"),
                        new FieldViolation("Booking.end", "must be after start")));
    }

    @Test
    void shouldNotThrowExceptionsWhenValidateForCreationWithProperParams() {
        LocalDateTime now = LocalDateTime.now();
        String start = now.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String end = now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        BookingDto properBooking = new BookingDto(1L,start, end);

        assertDoesNotThrow(() -> BookingValidator.validateForCreation(properBooking));
    }

    @Test
    void shouldThrowUnsupportedStateExceptionWhenValidateBookingStateWithUnsupportedState() {
        UnsupportedStateException unsupportedStateException = assertThrows(UnsupportedStateException.class,
                () -> BookingValidator.validateBookingState("UNSUPPORTED_STATE"));
        assertEquals("Unknown state: UNSUPPORTED_STATE", unsupportedStateException.getMessage());
    }

    @Test
    void shouldNotThrowUnsupportedStateExceptionWhenValidateBookingStateWithSupportedState() {
        assertDoesNotThrow(() -> BookingValidator.validateBookingState("CURRENT"));
    }
}