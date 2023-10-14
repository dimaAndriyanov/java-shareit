package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.shareit.request.ItemRequestValidator.*;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsByCreatorId(@RequestHeader(HEADER_USER_ID) Long creatorId) {
        log.info("Request on getting own item requests by user with id = {} has been received", creatorId);
        return itemRequestClient.getOwnRequests(creatorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequestsByUserId(@RequestHeader(HEADER_USER_ID) Long userId,
                                                           @RequestParam(required = false) @PositiveOrZero Integer from,
                                                           @RequestParam(required = false) @Positive Integer size) {
        log.info("Request on getting all item requests by user with id = {} " +
                "with page parameters from = {} and size = {} has been received", userId, from, size);
        return itemRequestClient.getAllRequests(userId, from == null ? 0 : from, size == null ? 10 : size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable Long requestId,
                                             @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting item request with id = {} from user with id = {} has been received",
                requestId, userId);
        return itemRequestClient.getRequest(requestId, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader(HEADER_USER_ID) Long creatorId) {
        log.info("Request on posting item request with\ndescription = {}\nfrom user with id = {} has been received",
                itemRequestDto.getDescription(), creatorId);
        validateItemRequestDto(itemRequestDto);
        itemRequestDto.setCreated(LocalDateTime.now().format(formatter));
        return itemRequestClient.postRequest(itemRequestDto, creatorId);
    }
}