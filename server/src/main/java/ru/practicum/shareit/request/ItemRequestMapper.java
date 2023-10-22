package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto result = new ItemRequestDto(
                itemRequest.getDescription(),
                itemRequest.getCreated().format(formatter)
        );
        result.setId(itemRequest.getId());
        return result;
    }

    public List<ItemRequestDto> toItemRequestDto(List<ItemRequest> itemRequests) {
        return itemRequests.stream().map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, LocalDateTime created, User creator) {
        return new ItemRequest(itemRequestDto.getDescription(), created, creator);
    }
}