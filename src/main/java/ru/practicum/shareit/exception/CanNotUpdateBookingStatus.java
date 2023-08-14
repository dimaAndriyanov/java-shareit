package ru.practicum.shareit.exception;

public class CanNotUpdateBookingStatus extends RuntimeException {
    public CanNotUpdateBookingStatus(String message) {
        super(message);
    }
}