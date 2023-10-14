package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class ItemRequestDto {
    private final String description;
    private String created;

    ItemRequestDto() {
        description = null;
        created = null;
    }
}