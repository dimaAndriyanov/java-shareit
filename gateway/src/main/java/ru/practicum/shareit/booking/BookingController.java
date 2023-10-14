package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


import static ru.practicum.shareit.booking.BookingValidator.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookingByIdAndUserId(@PathVariable Long id,
                                                          @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting booking with id = {} by user with id = {} has been received", id, userId);
        return bookingClient.getBooking(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByStateAndBookerId(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader(HEADER_USER_ID) Long bookerId,
            @RequestParam(required = false) @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size
    ) {
        log.info("Request on getting own bookings by state = \"{}\" by booker with id = {} " +
                        "with page parameters from = {} and size = {} has been received",
                state, bookerId, from, size);
        return bookingClient.getBookersBookings(validateBookingState(state).name(), bookerId,
                from == null ? 0 : from, size == null ? 10 : size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByStateAndOwnerId(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader(HEADER_USER_ID) Long ownerId,
            @RequestParam(required = false) @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size
    ) {
        log.info("Request on getting bookings on own items by state = \"{}\" by owner with id = {} " +
                        "with page parameters from = {} and size = {} has been received",
                state, ownerId, from, size);
        return bookingClient.getBookingsOfOwnItems(validateBookingState(state).name(), ownerId,
                from == null ? 0 : from, size == null ? 10 : size);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createBooking(@RequestBody ReceivedBookingDto bookingDto,
                                        @RequestHeader(HEADER_USER_ID) Long bookerId) {
        log.info("Request on posting booking with\nitemId = {}\nstartDate = {}\nendDate = {}\nhas been received",
                bookingDto.getItemId(),
                bookingDto.getStart(),
                bookingDto.getEnd());
        validateForCreation(bookingDto);
        return bookingClient.postBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateBookingStatus(@PathVariable Long id,
                                              @RequestHeader(HEADER_USER_ID) Long userId,
                                              @RequestParam Boolean approved) {
        return bookingClient.patchBooking(id, userId, approved);
    }
}