package ru.practicum.shareit.exception;

public class BookingDatesIntersectWithAlreadyExistingBookingException extends RuntimeException {
    public BookingDatesIntersectWithAlreadyExistingBookingException(String message) {
        super(message);
    }
}