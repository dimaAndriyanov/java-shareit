package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserValidator {
    public void validateForCreation(UserDto userDto) {
        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                fieldViolations.add(new FieldViolation("User.name", "must not be empty"));
            }
        } else {
            fieldViolations.add(new FieldViolation("User.name", "must not be null"));
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                fieldViolations.add(new FieldViolation("User.email", "must not be empty"));
            } else if (!userDto.getEmail()
                    .matches("^([\\w\\-]+\\.?)+[\\w\\-]+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]+$")) {
                fieldViolations.add(new FieldViolation("User.email", "must be real email address"));
            }
        } else {
            fieldViolations.add(new FieldViolation("User.email", "must not be null"));
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }

    public void validateForUpdating(UserDto userDto) {
        if (userDto.getName() == null && userDto.getEmail() == null) {
            throw new EmptyObjectException("Updated user must have at least one not null field");
        }

        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (userDto.getName() != null && userDto.getName().isBlank()) {
            fieldViolations.add(new FieldViolation("User.name", "must not be empty"));
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                fieldViolations.add(new FieldViolation("User.email", "must not be empty"));
            } else if (!userDto.getEmail()
                    .matches("^([\\w\\-]+\\.?)+[\\w\\-]+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]+$")) {
                fieldViolations.add(new FieldViolation("User.email", "must be real email address"));
            }
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }
}