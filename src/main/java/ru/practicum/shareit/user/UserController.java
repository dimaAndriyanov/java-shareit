package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserValidator userValidator;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        userValidator.validateForCreation(user);
        return userService.createUser(user);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@RequestBody User user, @PathVariable Long userId) {
        userValidator.validateForUpdating(user);
        return userService.updateUser(user, userId);
    }

    @DeleteMapping("/{userId}")
    public User deleteUser(@PathVariable Long userId) {
        return userService.deleteUserById(userId);
    }

    @DeleteMapping
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }
}