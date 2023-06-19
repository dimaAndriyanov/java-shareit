package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserValidator {
    public void validateForCreation(User user) {
        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (user.getName() != null) {
            if (user.getName().isBlank()) {
                fieldViolations.add(new FieldViolation("User.name", "must not be empty"));
            }
        } else {
            fieldViolations.add(new FieldViolation("User.name", "must not be null"));
        }

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                fieldViolations.add(new FieldViolation("User.email", "must not be empty"));
            } else if (!user.getEmail()
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

    public void validateForUpdating(User user) {
        if (user.getName() == null && user.getEmail() == null) {
            throw new EmptyObjectException("Updated user must have at least one not null field");
        }

        List<FieldViolation> fieldViolations = new ArrayList<>();

        if (user.getName() != null && user.getName().isBlank()) {
            fieldViolations.add(new FieldViolation("User.name", "must not be empty"));
        }

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                fieldViolations.add(new FieldViolation("User.email", "must not be empty"));
            } else if (!user.getEmail()
                    .matches("^([\\w\\-]+\\.?)+[\\w\\-]+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]+$")) {
                fieldViolations.add(new FieldViolation("User.email", "must be real email address"));
            }
        }

        if (!fieldViolations.isEmpty()) {
            throw new FieldValidationException(fieldViolations);
        }
    }
}