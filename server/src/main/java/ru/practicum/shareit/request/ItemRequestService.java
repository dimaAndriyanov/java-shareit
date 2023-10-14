package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRequestService {
    List<ItemRequestDto> getAllItemRequestsByCreatorId(Long creatorId);

    List<ItemRequestDto> getAllItemRequestsByUserId(Long userId, Integer from, Integer size);

    ItemRequestDto getItemRequestById(Long requestId, Long userId);

    ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long creatorId, LocalDateTime created);
}