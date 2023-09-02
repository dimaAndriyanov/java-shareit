package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto result = new ItemRequestDto(
                itemRequest.getDescription(),
                itemRequest.getCreated()
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