package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import static ru.practicum.shareit.user.UserValidator.*;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Request on getting all users has been received");
        return userClient.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("Request on getting user with id = {} has been received", userId);
        return userClient.getUser(userId);
    }

    @PostMapping
    //@ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
        log.info("Request on posting user with\nname = {}\nemail = {}\nhas been received",
                userDto.getName(),
                userDto.getEmail());
        validateForCreation(userDto);
        return userClient.postUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Request on patching user with\nid = {}\nname = {}\nemail = {}\nhas been received",
                userId,
                userDto.getName(),
                userDto.getEmail());
        validateForUpdating(userDto);
        return userClient.patchUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Request on deleting user with id = {} has been received", userId);
        return userClient.deleteUser(userId);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAllUsers() {
        log.info("Request on deleting all users has been received");
        return userClient.deleteAllUsers();
    }
}