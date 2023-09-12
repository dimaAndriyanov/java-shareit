package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;

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
@WebMvcTest(controllers = {BookingController.class, ItemController.class,
        ItemRequestController.class, UserController.class})
class ErrorHandlerTest {
    @MockBean
    private final BookingService bookingService;

    @MockBean
    private final ItemService itemService;

    @MockBean
    private final ItemRequestService itemRequestService;

    @MockBean
    private final UserService userService;

    private final MockMvc mvc;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(12).format(formatter);

    private final String end = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1).format(formatter);
    
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBodyValidationErrorWithCaughtFieldValidationException() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new FieldValidationException(List.of(new FieldViolation("testFieldName", "testMessage"))));

        mvc.perform(post("/users")
                        .content("{\"name\":\"userName\",\"email\":\"email@mail.com\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("testFieldName")))
                .andExpect(jsonPath("$[0].message", is("testMessage")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleVariablesValidationErrorWithCaughtConstraintViolationException() throws Exception {
        mvc.perform(get("/items?from={from}&size={size}", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemsByOwnerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtHttpMessageNotReadableException() throws Exception {
        mvc.perform(post("/bookings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Required request body is missing: " +
                        "public ru.practicum.shareit.booking.dto.SentBookingDto " +
                        "ru.practicum.shareit.booking.BookingController." +
                        "createBooking(ru.practicum.shareit.booking.dto.ReceivedBookingDto,java.lang.Long)")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtMethodArgumentTypeMismatchException() throws Exception {
        mvc.perform(get("/users/{userId}", "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Failed to convert value of type 'java.lang.String' " +
                        "to required type 'java.lang.Long'; " +
                        "nested exception is java.lang.NumberFormatException: For input string: \"abc\"")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtMissingRequestHeaderException() throws Exception {
        mvc.perform(get("/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Required request header 'X-Sharer-User-Id' " +
                        "for method parameter type Long is not present")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtMissingServletRequestParameterException() throws Exception {
        mvc.perform(get("/items/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Required request parameter 'text' " +
                        "for method parameter type String is not present")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtNotAvailableItemException() throws Exception {
        when(bookingService.createBooking(any(), any(),any()))
                .thenThrow(new NotAvailableItemException("notAvailableItemException"));

        mvc.perform(post("/bookings")
                        .content("{\"itemId\":1,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("notAvailableItemException")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtCanNotUpdateBookingStatusException() throws Exception {
        when(bookingService.updateBookingStatus(any(), any(), any()))
                .thenThrow(new CanNotUpdateBookingStatusException("canNotUpdateBookingStatusException"));

        mvc.perform(patch("/bookings/17?approved=true")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("canNotUpdateBookingStatusException")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtUnsupportedStateException() throws Exception {
        when(bookingService.getBookingsByStateAndBookerId(any(), any(), any(), any()))
                .thenThrow(new UnsupportedStateException("unsupportedStateException"));

        mvc.perform(get("/bookings?state=ALL")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("unsupportedStateException")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtPostingCommentWithoutCompletedBookingException() throws Exception {
        when(itemService.createComment(any(), any(), any(), any()))
                .thenThrow(new PostingCommentWithoutCompletedBookingException(
                        "postingCommentWithoutCompletedBookingException"));

        mvc.perform(post("/items/17/comment")
                        .content("{\"text\":\"text\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("postingCommentWithoutCompletedBookingException")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleEmptyObjectErrorWithCaughtEmptyObjectException() throws Exception {
        mvc.perform(patch("/users/17")
                        .content("{}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Updated user must have at least one not null field")));
    }

    @Test
    void shouldReturnConflictAndErrorWhenHandlePuttingConflictingDataErrorWithCaughtEmailIsAlreadyInUseException() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new EmailIsAlreadyInUseException("emailIsAlreadyInUseException"));

        mvc.perform(post("/users")
                        .content("{\"name\":\"userName\",\"email\":\"email@mail.com\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.error", is("emailIsAlreadyInUseException")));
    }

    @Test
    void shouldReturnConflictAndErrorWhenHandlePuttingConflictingDataErrorWithCaughtBookingDatesIntersectWithAlreadyExistingBookingException() throws Exception {
        when(bookingService.createBooking(any(), any(),any()))
                .thenThrow(new BookingDatesIntersectWithAlreadyExistingBookingException(
                        "bookingDatesIntersectWithAlreadyExistingBookingException"));

        mvc.perform(post("/bookings")
                        .content("{\"itemId\":1,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.error", is("bookingDatesIntersectWithAlreadyExistingBookingException")));
    }

    @Test
    void shouldReturnNotFoundAndErrorWhenHandleObjectNotFoundErrorWithCaughtObjectNotFoundException() throws Exception {
        when(itemRequestService.getItemRequestById(any(), any()))
                .thenThrow(new ObjectNotFoundException("objectNotFoundException"));

        mvc.perform(get("/requests/42")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.error", is("objectNotFoundException")));
    }

    @Test
    void shouldReturnForbiddenWhenHandleDataAccessExceptionWithCaughtDataAccessException() throws Exception {
        when(itemService.deleteItemById(any(), any()))
                .thenThrow(new DataAccessException("dataAccessException"));

        mvc.perform(delete("/items/42")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.error", is("dataAccessException")));
    }

    @Test
    void shouldReturnInternalServerErrorAndErrorWhenHandleInternalServerErrorWithNullPointerException() throws Exception {
        when(itemRequestService.getItemRequestById(any(), any()))
                .thenThrow(new NullPointerException("nullPointerException"));

        mvc.perform(get("/requests/42")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.error", is("nullPointerException")));
    }
}