package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private final String text;
    private final String authorName;
    private final String created;
}