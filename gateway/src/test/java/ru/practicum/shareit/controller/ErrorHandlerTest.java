package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

import java.nio.charset.StandardCharsets;

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
    private final UserClient userClient;

    @MockBean
    private final BookingClient bookingClient;

    @MockBean
    private final ItemClient itemClient;

    @MockBean
    private final ItemRequestClient itemRequestClient;

    private final MockMvc mvc;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnBadRequestAndErrorWhenHandleBodyValidationErrorWithCaughtFieldValidationException() throws Exception {
        mvc.perform(post("/users")
                        .content("{\"name\":\"\",\"email\":\"not_email_address\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("User.name")))
                .andExpect(jsonPath("$[0].message", is("must not be empty")))
                .andExpect(jsonPath("$[1].fieldName", is("User.email")))
                .andExpect(jsonPath("$[1].message", is("must be real email address")));
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
                        "public org.springframework.http.ResponseEntity<java.lang.Object> " +
                        "ru.practicum.shareit.booking.BookingController.createBooking(ru.practicum.shareit.booking.dto.BookingDto,java.lang.Long)")));
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
    void shouldReturnBadRequestAndErrorWhenHandleBadRequestErrorWithCaughtUnsupportedStateException() throws Exception {
        mvc.perform(get("/bookings?state=UNSUPPORTED_STATE")
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error", is("Unknown state: UNSUPPORTED_STATE")));
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
    void shouldReturnInternalServerErrorAndErrorWhenHandleInternalServerErrorWithNullPointerException() throws Exception {
        when(userClient.getUser(any()))
                .thenThrow(new NullPointerException("nullPointerException"));

        mvc.perform(get("/users/42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.error", is("nullPointerException")));
    }
}