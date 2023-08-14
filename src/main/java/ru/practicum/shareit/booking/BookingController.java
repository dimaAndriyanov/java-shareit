package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.UnsupportedState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.BookingValidator.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public SentBookingDto getBookingByIdAndUserId(@PathVariable Long id,
                                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Request on getting booking with id = {} by user with id = {} has been received", id, userId);
        return bookingService.getBookingByIdAndUserId(id, userId);
    }

    @GetMapping
    public List<SentBookingDto> getBookingsByStateAndBookerId(@RequestParam(defaultValue = "ALL") String state,
                                                            @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        log.info("Request on getting own bookings by state = \"{}\" by booker with id = {} has been received",
                state, bookerId);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedState("Unknown state: " + state);
        }
        return bookingService.getBookingsByStateAndBookerId(bookingState, bookerId);
    }

    @GetMapping("/owner")
    public List<SentBookingDto> getBookingsByStateAndOwnerId(@RequestParam(defaultValue = "ALL") String state,
                                                            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Request on getting bookings on own items by state = \"{}\" by owner with id = {} has been received",
                state, ownerId);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedState("Unknown state: " + state);
        }
        return bookingService.getBookingsByStateAndOwnerId(bookingState, ownerId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public SentBookingDto createBooking(@RequestBody ReceivedBookingDto bookingDto,
                                        @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        log.info("Request on posting booking with\nitemId = {}\nstartDate = {}\nendDate = {}\nhas been received",
                bookingDto.getItemId(),
                bookingDto.getStart(),
                bookingDto.getEnd());
        List<LocalDateTime> dates =  validateForCreation(bookingDto);
        return bookingService.createBooking(bookingDto.getItemId(), bookerId, dates);
    }

    @PatchMapping("/{id}")
    public SentBookingDto updateBookingStatus(@PathVariable Long id,
                                              @RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam Boolean approved) {
        return bookingService.updateBookingStatus(id, userId, approved);
    }
}