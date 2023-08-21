package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;

@UtilityClass
public class CommentValidator {
    public void validateCommentDto(CommentDto commentDto) {
        if (commentDto.getText() == null) {
            throw new FieldValidationException(List.of(new FieldViolation("Comment.text", "must not be null")));
        }
        if (commentDto.getText().isBlank()) {
            throw new FieldValidationException(List.of(new FieldViolation("Comment.text", "must not be empty")));
        }
    }
}