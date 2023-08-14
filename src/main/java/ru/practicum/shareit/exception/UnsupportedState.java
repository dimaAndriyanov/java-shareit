package ru.practicum.shareit.exception;

public class UnsupportedState extends RuntimeException {
    public UnsupportedState(String message) {
        super(message);
    }
}