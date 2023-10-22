package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @GetMapping("/{id}")
    public SentBookingDto getBookingByIdAndUserId(@PathVariable Long id,
                                                  @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting booking with id = {} by user with id = {} has been received", id, userId);
        return bookingService.getBookingByIdAndUserId(id, userId);
    }

    @GetMapping
    public List<SentBookingDto> getBookingsByStateAndBookerId(
            @RequestParam String state,
            @RequestHeader(HEADER_USER_ID) Long bookerId,
            @RequestParam Integer from,
            @RequestParam Integer size
    ) {
        log.info("Request on getting own bookings by state = \"{}\" by booker with id = {} " +
                        "with page parameters from = {} and size = {} has been received",
                state, bookerId, from, size);
        return bookingService.getBookingsByStateAndBookerId(BookingState.valueOf(state), bookerId, from, size);
    }

    @GetMapping("/owner")
    public List<SentBookingDto> getBookingsByStateAndOwnerId(
            @RequestParam String state,
            @RequestHeader(HEADER_USER_ID) Long ownerId,
            @RequestParam Integer from,
            @RequestParam Integer size
    ) {
        log.info("Request on getting bookings on own items by state = \"{}\" by owner with id = {} " +
                        "with page parameters from = {} and size = {} has been received",
                state, ownerId, from, size);
        return bookingService.getBookingsByStateAndOwnerId(BookingState.valueOf(state), ownerId, from, size);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public SentBookingDto createBooking(@RequestBody ReceivedBookingDto bookingDto,
                                        @RequestHeader(HEADER_USER_ID) Long bookerId) {
        log.info("Request on posting booking with\nitemId = {}\nstartDate = {}\nendDate = {}\nhas been received",
                bookingDto.getItemId(),
                bookingDto.getStart(),
                bookingDto.getEnd());
        return bookingService.createBooking(
                bookingDto.getItemId(),
                bookerId,
                List.of(
                        LocalDateTime.parse(bookingDto.getStart()),
                        LocalDateTime.parse(bookingDto.getEnd())
                )
        );
    }

    @PatchMapping("/{id}")
    public SentBookingDto updateBookingStatus(@PathVariable Long id,
                                              @RequestHeader(HEADER_USER_ID) Long userId,
                                              @RequestParam Boolean approved) {
        return bookingService.updateBookingStatus(id, userId, approved);
    }
}