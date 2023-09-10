package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.ItemRequestMapper.*;
import static ru.practicum.shareit.item.ItemMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    @Qualifier("userRepositoryDbImpl")
    private final UserRepository userRepository;

    @Qualifier("itemRepositoryDbImpl")
    private final ItemRepository itemRepository;

    @Override
    public List<ItemRequestDto> getAllItemRequestsByCreatorId(Long creatorId) {
        userRepository.checkForPresenceById(creatorId);
        List<ItemRequestDto> foundItemRequests =
                toItemRequestDto(itemRequestRepository.findAllByCreatorIdOrderByCreatedDesc(creatorId));
        return addItemsToRequestsDto(foundItemRequests);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestsByUserId(Long userId, Integer from, Integer size) {
        userRepository.checkForPresenceById(userId);
        PageRequest page = PageRequest.of(from / size, size);
        List<ItemRequestDto> foundItemRequests =
                toItemRequestDto(itemRequestRepository.findAllByCreatorIdNotOrderByCreatedDesc(userId, page)
                        .getContent());
        return addItemsToRequestsDto(foundItemRequests);
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        userRepository.checkForPresenceById(userId);
        Optional<ItemRequest> possibleItemRequest = itemRequestRepository.findById(requestId);
        if (possibleItemRequest.isPresent()) {
            ItemRequestDto result = toItemRequestDto(possibleItemRequest.get());
            addItemsToRequestDto(result, toItemDto(itemRepository.getAllItemsByRequestId(result.getId())));
            return result;
        } else throw new ObjectNotFoundException(String.format("Item request with id = %s not found", requestId));
    }

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long creatorId, LocalDateTime created) {
        userRepository.checkForPresenceById(creatorId);
        User creator = userRepository.getById(creatorId);
        ItemRequest result = itemRequestRepository.save(toItemRequest(itemRequestDto, created, creator));
        log.info("Item request with id = {} has been created", result.getId());
        return toItemRequestDto(result);
    }

    private void addItemsToRequestDto(ItemRequestDto itemRequestDto, List<ItemDto> itemsDto) {
        itemsDto.forEach(itemRequestDto::addItemDto);
    }

    private List<ItemRequestDto> addItemsToRequestsDto(List<ItemRequestDto> itemRequests) {
        List<Long> itemRequestsIds = itemRequests.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());
        List<ItemDto> items = toItemDto(itemRepository.getAllItemsByRequestIds(itemRequestsIds));
        Map<Long, List<ItemDto>> itemsByItemRequestsIds = new HashMap<>();
        for (ItemRequestDto itemRequest : itemRequests) {
            itemsByItemRequestsIds.put(itemRequest.getId(), new ArrayList<>());
        }
        for (ItemDto item : items) {
            itemsByItemRequestsIds.get(item.getRequestId()).add(item);
        }
        itemRequests.forEach(itemRequestDto -> addItemsToRequestDto(itemRequestDto,
                itemsByItemRequestsIds.get(itemRequestDto.getId())));
        return itemRequests;
    }
}