package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentMapper {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public CommentDto toCommentDto(Comment comment) {
        CommentDto result =
                new CommentDto(comment.getText(), comment.getAuthorName(), comment.getCreated().format(formatter));
        result.setId(comment.getId());
        return result;
    }

    public List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public Comment toComment(CommentDto commentDto, Item item, String authorName, LocalDateTime created) {
        return new Comment(item, commentDto.getText(), authorName, created);
    }
}