package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
import ru.practicum.shareit.user.dto.UserDtoForBooking;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private final ObjectMapper mapper;

    @MockBean
    private final BookingService bookingService;

    private final MockMvc mvc;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ReceivedBookingDto receivedBookingDto = new ReceivedBookingDto(
            27L,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(12).format(formatter),
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1).format(formatter)
    );

    private final SentBookingDto sentBookingDto = new SentBookingDto(
            17L,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(12).format(formatter),
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1).format(formatter),
            BookingStatus.APPROVED,
            new UserDtoForBooking(23L),
            new ItemDtoForBooking(27L, "itemName")
    );

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnOkAndBookingDtoWhenGetBookingByIdAndUserId() throws Exception {
        when(bookingService.getBookingByIdAndUserId(any(), any()))
                .thenReturn(sentBookingDto);

        mvc.perform(get("/bookings/{id}", 27)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sentBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(sentBookingDto.getStart())))
                .andExpect(jsonPath("$.end", is(sentBookingDto.getEnd())))
                .andExpect(jsonPath("$.status", is(sentBookingDto.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(sentBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(sentBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(sentBookingDto.getItem().getName())));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndBookerIdWithFromLessThanZero() throws Exception {
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndBookerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndBookerIdWithSizeLessThanOrEqualToZero() throws Exception {
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", 0, 0)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndBookerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }

    @Test
    void shouldReturnOkAndListOfBookingDtosWhenGetBookingsByStateAndBookerId() throws Exception {
        when(bookingService.getBookingsByStateAndBookerId(any(), any(), any(), any()))
                .thenReturn(List.of(sentBookingDto));

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", 0, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(sentBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(sentBookingDto.getStart())))
                .andExpect(jsonPath("$[0].end", is(sentBookingDto.getEnd())))
                .andExpect(jsonPath("$[0].status", is(sentBookingDto.getStatus().name())))
                .andExpect(jsonPath("$[0].booker.id", is(sentBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(sentBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(sentBookingDto.getItem().getName())));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndOwnerIdWithFromLessThanZero() throws Exception {
        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndOwnerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndOwnerIdWithSizeLessThanOrEqualToZero() throws Exception {
        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", 0, 0)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndOwnerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }

    @Test
    void shouldReturnOkAndListOfBookingsWhenGetBookingsByStateAndOwnerId() throws Exception {
        when(bookingService.getBookingsByStateAndOwnerId(any(), any(), any(), any()))
                .thenReturn(List.of(sentBookingDto));

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", 0, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(sentBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(sentBookingDto.getStart())))
                .andExpect(jsonPath("$[0].end", is(sentBookingDto.getEnd())))
                .andExpect(jsonPath("$[0].status", is(sentBookingDto.getStatus().name())))
                .andExpect(jsonPath("$[0].booker.id", is(sentBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(sentBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(sentBookingDto.getItem().getName())));
    }

    @Test
    void shouldReturnCreatedAndBookingDtoWheCreateBooking() throws Exception {
        when(bookingService.createBooking(any(), any(), any()))
                .thenReturn(sentBookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(receivedBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id", is(sentBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(sentBookingDto.getStart())))
                .andExpect(jsonPath("$.end", is(sentBookingDto.getEnd())))
                .andExpect(jsonPath("$.status", is(sentBookingDto.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(sentBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(sentBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(sentBookingDto.getItem().getName())));
    }

    @Test
    void shouldReturnOkAndBookingWhenUpdateBookingStatus() throws Exception {
        when(bookingService.updateBookingStatus(any(), any(), any()))
                .thenReturn(sentBookingDto);

        mvc.perform(patch("/bookings/{id}?approved={approved}", 27, true)
                        .header(HEADER_USER_ID, 42)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sentBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(sentBookingDto.getStart())))
                .andExpect(jsonPath("$.end", is(sentBookingDto.getEnd())))
                .andExpect(jsonPath("$.status", is(sentBookingDto.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(sentBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(sentBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(sentBookingDto.getItem().getName())));
    }
}