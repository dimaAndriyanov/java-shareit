package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.*;

class ItemValidatorTest {
    @Test
    void validateItemForCreation() {
        ItemDto itemWithNullNameEmptyDescriptionAndNullAvailable = new ItemDto(null, "   ", null, null, null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> ItemValidator.validateItemForCreation(itemWithNullNameEmptyDescriptionAndNullAvailable));
        assertEquals(3, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.name", "must not be null")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.description", "must not be empty")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.available", "must not be null")
        ));

        ItemDto itemWithEmptyNameNullDescriptionAndProperAvailable = new ItemDto("", null, true, null, null);
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> ItemValidator.validateItemForCreation(itemWithEmptyNameNullDescriptionAndProperAvailable));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.name", "must not be empty")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.description", "must not be null")
        ));

        ItemDto properItem = new ItemDto("properName", "properDescription", true, null, null);
        assertDoesNotThrow(() -> ItemValidator.validateItemForCreation(properItem));
    }

    @Test
    void validateItemForUpdating() {
        ItemDto itemWithNullNameNullDescriptionAndNullAvailable = new ItemDto(null, null, null, null, null);
        EmptyObjectException emptyObjectException = assertThrows(EmptyObjectException.class,
                () -> ItemValidator.validateItemForUpdating(itemWithNullNameNullDescriptionAndNullAvailable));
        assertEquals("Updated item must have at least one not null field", emptyObjectException.getMessage());

        ItemDto itemWithEmptyNameEmptyDescriptionAndNullAvailable = new ItemDto("   ", "", null, null, null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> ItemValidator.validateItemForUpdating(itemWithEmptyNameEmptyDescriptionAndNullAvailable));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.name", "must not be empty")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.description", "must not be empty")
        ));

        ItemDto properItemWithNotNullName = new ItemDto("properName", null, null, null, null);
        assertDoesNotThrow(() -> ItemValidator.validateItemForUpdating(properItemWithNotNullName));

        ItemDto properItemWithNotNullDescription = new ItemDto(null, "properDescription", null, null, null);
        assertDoesNotThrow(() -> ItemValidator.validateItemForUpdating(properItemWithNotNullDescription));

        ItemDto properItemWithNotNullAvailable = new ItemDto(null, null, true, null, null);
        assertDoesNotThrow(() -> ItemValidator.validateItemForUpdating(properItemWithNotNullAvailable));

        ItemDto properItemWithNoNullFields = new ItemDto("properName", "properDescription", true, null, null);
        assertDoesNotThrow(() -> ItemValidator.validateItemForUpdating(properItemWithNoNullFields));
    }
}