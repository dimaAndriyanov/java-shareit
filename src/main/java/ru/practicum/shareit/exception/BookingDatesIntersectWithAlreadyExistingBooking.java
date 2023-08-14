package ru.practicum.shareit.exception;

public class BookingDatesIntersectWithAlreadyExistingBooking extends RuntimeException {
    public BookingDatesIntersectWithAlreadyExistingBooking(String message) {
        super(message);
    }
}