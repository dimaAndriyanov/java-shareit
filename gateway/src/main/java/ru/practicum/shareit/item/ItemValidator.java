package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemValidator {
    public void validateItemForCreation(ItemDto itemDto) {
        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                fieldViolations.add(new FieldViolation("Item.name", "must not be empty"));
            }
        } else {
            fieldViolations.add(new FieldViolation("Item.name", "must not be null"));
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                fieldViolations.add(new FieldViolation("Item.description", "must not be empty"));
            }
        } else {
            fieldViolations.add(new FieldViolation("Item.description", "must not be null"));
        }

        if (itemDto.getAvailable() == null) {
            fieldViolations.add(new FieldViolation("Item.available", "must not be null"));
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }

    public void validateItemForUpdating(ItemDto itemDto) {
        if (itemDto.getName() == null &&
                itemDto.getDescription() == null &&
                itemDto.getAvailable() == null &&
                itemDto.getRequestId() == null) {
            throw new EmptyObjectException("Updated item must have at least one not null field");
        }

        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                fieldViolations.add(new FieldViolation("Item.name", "must not be empty"));
            }
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                fieldViolations.add(new FieldViolation("Item.description", "must not be empty"));
            }
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }
}