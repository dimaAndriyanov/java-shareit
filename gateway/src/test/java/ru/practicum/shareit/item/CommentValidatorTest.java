package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.item.dto.CommentDto;

import static org.junit.jupiter.api.Assertions.*;

class CommentValidatorTest {

    @Test
    void shouldThrowFieldValidationExceptionWhenValidateCommentDtoWithWrongFields() {
        CommentDto commentWithNullText = new CommentDto(null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> CommentValidator.validateCommentDto(commentWithNullText));
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertEquals(new FieldViolation("Comment.text", "must not be null"),
                fieldValidationException.getFieldViolations().get(0));

        CommentDto commentWithEmptyText = new CommentDto("   ");
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> CommentValidator.validateCommentDto(commentWithEmptyText));
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertEquals(new FieldViolation("Comment.text", "must not be empty"),
                fieldValidationException.getFieldViolations().get(0));
    }

    @Test
    void shouldNotThrowExceptionsWhenWhenValidateCommentDtoWithProperFields() {
        CommentDto commentWithProperFields = new CommentDto("proper comment");

        assertDoesNotThrow(() -> CommentValidator.validateCommentDto(commentWithProperFields));
    }
}