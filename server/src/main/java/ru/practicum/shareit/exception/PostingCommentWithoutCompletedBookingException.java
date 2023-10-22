package ru.practicum.shareit.exception;

public class PostingCommentWithoutCompletedBookingException extends RuntimeException {
    public PostingCommentWithoutCompletedBookingException(String message) {
        super(message);
    }
}