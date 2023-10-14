package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Request on getting all users has been received");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Request on getting user with id = {} has been received", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Request on posting user with\nname = {}\nemail = {}\nhas been received",
                userDto.getName(),
                userDto.getEmail());
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Request on patching user with\nid = {}\nname = {}\nemail = {}\nhas been received",
                userId,
                userDto.getName(),
                userDto.getEmail());
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public UserDto deleteUser(@PathVariable Long userId) {
        log.info("Request on deleting user with id = {} has been received", userId);
        return userService.deleteUserById(userId);
    }

    @DeleteMapping
    public void deleteAllUsers() {
        log.info("Request on deleting all users has been received");
        userService.deleteAllUsers();
    }
}