package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ItemRequestDto {
    private final String description;
    private String created;

    ItemRequestDto() {
        description = null;
    }
}