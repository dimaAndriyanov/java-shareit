package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ItemRequestValidatorTest {

    @Test
    void validateItemRequestDto() {
        ItemRequestDto itemRequestWithNullDescription = new ItemRequestDto(null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> ItemRequestValidator.validateItemRequestDto(itemRequestWithNullDescription));
        assertThat(fieldValidationException.getFieldViolations(), hasSize(1));
        assertThat(fieldValidationException.getFieldViolations(),
                hasItem(new FieldViolation("ItemRequest.description", "must not be null")));

        ItemRequestDto itemRequestWithEmptyDescription = new ItemRequestDto("  ");
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> ItemRequestValidator.validateItemRequestDto(itemRequestWithEmptyDescription));
        assertThat(fieldValidationException.getFieldViolations(), hasSize(1));
        assertThat(fieldValidationException.getFieldViolations(),
                hasItem(new FieldViolation("ItemRequest.description", "must not be empty")));

        ItemRequestDto properItemRequest = new ItemRequestDto("description");
        assertDoesNotThrow(() -> ItemRequestValidator.validateItemRequestDto(properItemRequest));
    }
}