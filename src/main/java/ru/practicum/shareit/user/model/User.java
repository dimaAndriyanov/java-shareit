package ru.practicum.shareit.user.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private final String name;
    private final String email;
}