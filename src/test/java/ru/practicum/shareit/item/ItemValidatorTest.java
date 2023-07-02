package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemValidatorTest {
    private final ItemValidator itemValidator;

    @Autowired
    ItemValidatorTest(ItemValidator itemValidator) {
        this.itemValidator = itemValidator;
    }

    @Test
    void validateForCreation() {
        ItemDto itemWithNullNameEmptyDescriptionAndNullAvailable = new ItemDto(null, "   ", null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> itemValidator.validateForCreation(itemWithNullNameEmptyDescriptionAndNullAvailable));
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

        ItemDto itemWithEmptyNameNullDescriptionAndProperAvailable = new ItemDto("", null, true);
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> itemValidator.validateForCreation(itemWithEmptyNameNullDescriptionAndProperAvailable));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.name", "must not be empty")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.description", "must not be null")
        ));

        ItemDto properItem = new ItemDto("properName", "properDescription", true);
        assertDoesNotThrow(() -> itemValidator.validateForCreation(properItem));
    }

    @Test
    void validateForUpdating() {
        ItemDto itemWithNullNameNullDescriptionAndNullAvailable = new ItemDto(null, null, null);
        EmptyObjectException emptyObjectException = assertThrows(EmptyObjectException.class,
                () -> itemValidator.validateForUpdating(itemWithNullNameNullDescriptionAndNullAvailable));
        assertEquals("Updated item must have at least one not null field", emptyObjectException.getMessage());

        ItemDto itemWithEmptyNameEmptyDescriptionAndNullAvailable = new ItemDto("   ", "", null);
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> itemValidator.validateForUpdating(itemWithEmptyNameEmptyDescriptionAndNullAvailable));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.name", "must not be empty")
        ));
        assertTrue(fieldValidationException.getFieldViolations().contains(
                new FieldViolation("Item.description", "must not be empty")
        ));

        ItemDto properItemWithNotNullName = new ItemDto("properName", null, null);
        assertDoesNotThrow(() -> itemValidator.validateForUpdating(properItemWithNotNullName));

        ItemDto properItemWithNotNullDescription = new ItemDto(null, "properDescription", null);
        assertDoesNotThrow(() -> itemValidator.validateForUpdating(properItemWithNotNullDescription));

        ItemDto properItemWithNotNullAvailable = new ItemDto(null, null, true);
        assertDoesNotThrow(() -> itemValidator.validateForUpdating(properItemWithNotNullAvailable));

        ItemDto properItemWithNoNullFields = new ItemDto("properName", "properDescription", true);
        assertDoesNotThrow(() -> itemValidator.validateForUpdating(properItemWithNoNullFields));
    }
}