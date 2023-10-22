package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {
    @Test
    void validateForCreation() {
        UserDto userWithNullNameAndEmptyEmail = new UserDto(null, "");
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> UserValidator.validateForCreation(userWithNullNameAndEmptyEmail));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.name", "must not be null")));
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must not be empty")));

        UserDto userWithEmptyNameAndNullEmail = new UserDto(" ", null);
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> UserValidator.validateForCreation(userWithEmptyNameAndNullEmail));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.name", "must not be empty")));
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must not be null")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email@");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email@mail");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email@mail.");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_emailmail.ru");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", ".bad_email@mail.ru");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email.@mail.ru");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad..email@mail.ru");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> {
                    UserDto userWithProperNameAndBadEmail = new UserDto("John", "bad_email@.mail.ru");
                    UserValidator.validateForCreation(userWithProperNameAndBadEmail);
                });
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        UserDto userWithProperNameAndProperEmail = new UserDto("John", "my.very_proper-email@in-box.mail.com");
        assertDoesNotThrow(() -> UserValidator.validateForCreation(userWithProperNameAndProperEmail));
    }

    @Test
    void validateForUpdating() {
        UserDto userWithNullNameAndNullEmail = new UserDto(null, null);
        EmptyObjectException emptyObjectException = assertThrows(EmptyObjectException.class,
                () -> UserValidator.validateForUpdating(userWithNullNameAndNullEmail));
        assertEquals("Updated user must have at least one not null field", emptyObjectException.getMessage());

        UserDto userWithEmptyNameAndEmptyEmail = new UserDto("", " ");
        FieldValidationException fieldValidationException = assertThrows(FieldValidationException.class,
                () -> UserValidator.validateForUpdating(userWithEmptyNameAndEmptyEmail));
        assertEquals(2, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.name", "must not be empty")));
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must not be empty")));

        UserDto userWithNullNameAndBadEmail = new UserDto(null, "bad_email");
        fieldValidationException = assertThrows(FieldValidationException.class,
                () -> UserValidator.validateForUpdating(userWithNullNameAndBadEmail));
        assertEquals(1, fieldValidationException.getFieldViolations().size());
        assertTrue(fieldValidationException.getFieldViolations()
                .contains(new FieldViolation("User.email", "must be real email address")));

        UserDto userWithProperNameAndNullEmail = new UserDto("John", null);
        assertDoesNotThrow(() -> UserValidator.validateForUpdating(userWithProperNameAndNullEmail));
    }
}