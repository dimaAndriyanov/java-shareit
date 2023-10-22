package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemRequestDto> getAllItemRequestsByCreatorId(@RequestHeader(HEADER_USER_ID) Long creatorId) {
        log.info("Request on getting own item requests by user with id = {} has been received", creatorId);
        return itemRequestService.getAllItemRequestsByCreatorId(creatorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequestsByUserId(@RequestHeader(HEADER_USER_ID) Long userId,
                                                           @RequestParam Integer from,
                                                           @RequestParam Integer size) {
        log.info("Request on getting all item requests by user with id = {} " +
                "with page parameters from = {} and size = {} has been received", userId, from, size);
        return itemRequestService.getAllItemRequestsByUserId(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@PathVariable Long requestId,
                                             @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting item request with id = {} from user with id = {} has been received",
                requestId, userId);
        return itemRequestService.getItemRequestById(requestId, userId);
    }

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader(HEADER_USER_ID) Long creatorId) {
        log.info("Request on posting item request with\ndescription = {}\nfrom user with id = {} has been received",
                itemRequestDto.getDescription(), creatorId);
        return itemRequestService.createItemRequest(itemRequestDto, creatorId,
                LocalDateTime.parse(itemRequestDto.getCreated()));
    }
}