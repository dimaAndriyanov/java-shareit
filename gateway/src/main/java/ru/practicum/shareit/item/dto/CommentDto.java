package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class CommentDto {
    private final String text;
    private String created;
}