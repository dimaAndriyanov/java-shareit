package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@UtilityClass
public class ItemRequestValidator {
    public void validateItemRequestDto(ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null) {
            throw new FieldValidationException(
                    List.of(new FieldViolation("ItemRequest.description", "must not be null"))
            );
        }
        if (itemRequestDto.getDescription().isBlank()) {
            throw new FieldValidationException(
                    List.of(new FieldViolation("ItemRequest.description", "must not be empty"))
            );
        }
    }
}